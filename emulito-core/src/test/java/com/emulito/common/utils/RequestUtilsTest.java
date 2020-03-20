package com.emulito.common.utils;

import com.google.common.collect.Lists;
import com.emulito.common.domain.http.HttpRequestContainer;
import com.emulito.common.domain.http.HttpResponseContainer;
import com.emulito.common.exception.EmulatorException;
import com.emulito.common.service.EmulatorService;
import org.hamcrest.collection.IsMapContaining;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

public class RequestUtilsTest {

    private static final String TEST_HEADER_NAME = "TestHeader";
    private static final String TEST_HEADER_VALUE = "TestRequestHeaderValue";
    private static final String TEST_RESPONSE_BODY = "TestResponseBody";
    private static final String TEST_RESPONSE_HEADER_VALUE = "TestResponseHeaderValue";
    private static final String TEST_RESPONSE_HEADER_NAME = "TestResponseHeader";
    private static final String TEST_REQUEST_BODY = "TestRequestBody";
    private static final int TEST_RESPONSE_STATUS_CODE = 200;
    private static final String TEST_REQUEST_URL = "TestRequestUrl";
    private static final String TEST_HTTP_METHOD = "TEST_HTTP_METHOD";
    private static final String TEST_URL_PARAMETER_KEY = "testUrlParameterKey";

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private RequestUtils requestUtils = new RequestUtils();

    @Mock
    private HttpServletRequest httpServletRequest;

    @Mock
    private HttpServletResponse httpServletResponse;

    @Mock
    private PrintWriter printWriter;

    @Mock
    private EmulatorService mockEmulatorService;

    private Map<String, String[]> parameterMap;
    private ArrayList<String> headerNames;
    private ArrayList<String> headerValues;
    private BufferedReader bufferedReader;
    private HttpResponseContainer httpResponseContainer;

    @Before
    public void setup() throws IOException {
        given(httpServletRequest.getRequestURI()).willReturn(TEST_REQUEST_URL);
        given(httpServletRequest.getMethod()).willReturn(TEST_HTTP_METHOD);
        given(httpServletRequest.getQueryString()).willReturn(TEST_HTTP_METHOD);

        parameterMap = new HashMap<>();
        parameterMap.put(TEST_URL_PARAMETER_KEY, new String[]{"testUrlParameterValue"});
        given(httpServletRequest.getParameterMap()).willReturn(parameterMap);

        headerNames = new ArrayList<>();
        headerNames.add(TEST_HEADER_NAME);
        given(httpServletRequest.getHeaderNames()).willReturn(Collections.enumeration(headerNames));

        headerValues = new ArrayList<>();
        headerValues.add(TEST_HEADER_VALUE);
        given(httpServletRequest.getHeaders(TEST_HEADER_NAME)).willReturn(Collections.enumeration(headerValues));

        bufferedReader = new BufferedReader(new StringReader(TEST_REQUEST_BODY));
        given(httpServletRequest.getReader()).willReturn(bufferedReader);

        given(httpServletResponse.getWriter()).willReturn(printWriter);

        httpResponseContainer = new HttpResponseContainer();
        httpResponseContainer.setStatusCode(TEST_RESPONSE_STATUS_CODE);
        httpResponseContainer.setBody(TEST_RESPONSE_BODY);
        httpResponseContainer.addHeader(TEST_RESPONSE_HEADER_NAME, TEST_RESPONSE_HEADER_VALUE);
        given(mockEmulatorService.getResponse(any())).willReturn(httpResponseContainer);
    }

    @Test
    public void buildRequestContainer() {
        HttpRequestContainer httpRequestContainer = requestUtils.buildRequestContainer(httpServletRequest);

        assertThat(httpRequestContainer.getUrl(), is(TEST_REQUEST_URL));
        assertThat(httpRequestContainer.getHttpMethod(), is(TEST_HTTP_METHOD));
        assertThat(httpRequestContainer.getParams(), IsMapContaining.hasEntry(TEST_URL_PARAMETER_KEY, asList(parameterMap.get(TEST_URL_PARAMETER_KEY))));
        assertThat(httpRequestContainer.getHeaders(), IsMapContaining.hasEntry(TEST_HEADER_NAME, headerValues));
        assertThat(httpRequestContainer.getBody(), is(TEST_REQUEST_BODY));
    }

    @Test
    public void buildRequestContainerWithUriOverride() {
        HttpRequestContainer httpRequestContainer = requestUtils.buildRequestContainer(httpServletRequest, "http://localhost:8080/overrideContext/newBase?overrideParameterKey=overrideParameterValue");

        assertThat(httpRequestContainer.getUrl(), is("/overrideContext/newBase"));
        assertThat(httpRequestContainer.getHttpMethod(), is(TEST_HTTP_METHOD));
        assertThat(httpRequestContainer.getParams(), IsMapContaining.hasEntry("overrideParameterKey", Lists.newArrayList("overrideParameterValue")));
        assertThat(httpRequestContainer.getHeaders(), IsMapContaining.hasEntry(TEST_HEADER_NAME, headerValues));
        assertThat(httpRequestContainer.getBody(), is(TEST_REQUEST_BODY));
    }

    @Test
    public void buildRequestContainerIOException() throws IOException {
        given(httpServletRequest.getReader()).willThrow(new IOException());

        thrown.expect(EmulatorException.class);
        thrown.expectMessage("There was an error parsing the body of the application request");

        requestUtils.buildRequestContainer(httpServletRequest);
    }
}