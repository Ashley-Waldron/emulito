package com.emulito.common.web.controller;

import com.emulito.common.exception.ApplicationRequestNotFoundException;
import com.emulito.common.exception.EmulatorException;
import com.emulito.common.service.EmulatorService;
import com.emulito.common.utils.RequestUtils;
import com.google.common.collect.Lists;
import com.emulito.common.domain.http.HttpRequestContainer;
import com.emulito.common.domain.http.HttpResponseContainer;
import com.emulito.common.domain.http.HttpResponseRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.springframework.web.client.HttpClientErrorException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

public class EmulatorControllerTest {

    private static final String TEST_HEADER_NAME = "TestHeader";
    private static final String TEST_HEADER_VALUE = "TestRequestHeaderValue";
    private static final String TEST_RESPONSE_BODY = "TestResponseBody";
    private static final String TEST_RESPONSE_HEADER_VALUE = "TestResponseHeaderValue";
    private static final String TEST_RESPONSE_HEADER_NAME = "TestResponseHeader";
    private static final String TEST_REQUEST_BODY = "TestRequestBody";
    private static final int TEST_RESPONSE_STATUS_CODE = 200;
    private static final String TEST_REQUEST_URL = "TestRequestUrl";
    private static final String TEST_HTTP_METHOD = "TEST_HTTP_METHOD";
    private static final String CONTENT_LENGTH_HEADER_KEY = "content-length";
    private static final String X_REQUEST_URI_OVERRIDE_HEADER_KEY = "X-Request-URI-Override";
    private static final String TEST_REQUEST_NAME = "Test Request Name";

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @InjectMocks
    private EmulatorController emulatorController;

    @Mock
    private HttpServletRequest httpServletRequest;

    @Mock
    private HttpServletResponse httpServletResponse;

    @Mock
    private PrintWriter printWriter;

    @Mock
    private EmulatorService mockEmulatorService;

    @Mock
    private RequestUtils mockRequestUtils;

    private ArrayList<String> headerNames;
    private ArrayList<String> headerValues;
    private HttpResponseContainer httpResponseContainer;
    private HttpRequestContainer httpRequestContainer;
    private Map<String, List<String>> testUrlParams;
    private Map<String, List<String>> testHeaders;

    @Before
    public void setup() throws IOException {
        headerNames = new ArrayList<>();
        headerNames.add(TEST_HEADER_NAME);

        headerValues = new ArrayList<>();
        headerValues.add(TEST_HEADER_VALUE);
        testHeaders = new HashMap<>();
        testHeaders.put(TEST_HEADER_NAME, headerValues);

        given(httpServletResponse.getWriter()).willReturn(printWriter);

        httpRequestContainer = new HttpRequestContainer();
        httpRequestContainer.setUrl(TEST_REQUEST_URL);
        httpRequestContainer.setHttpMethod(TEST_HTTP_METHOD);
        httpRequestContainer.setParams(testUrlParams);
        httpRequestContainer.setHeaders(testHeaders);
        httpRequestContainer.setBody(TEST_REQUEST_BODY);
        given(mockRequestUtils.buildRequestContainer(httpServletRequest)).willReturn(httpRequestContainer);
        given(mockRequestUtils.buildRequestContainer(eq(httpServletRequest), anyString())).willReturn(httpRequestContainer);

        httpResponseContainer = new HttpResponseContainer();
        httpResponseContainer.setStatusCode(TEST_RESPONSE_STATUS_CODE);
        httpResponseContainer.setBody(TEST_RESPONSE_BODY);
        httpResponseContainer.addHeader(TEST_RESPONSE_HEADER_NAME, TEST_RESPONSE_HEADER_VALUE);
        given(mockEmulatorService.getResponse(httpRequestContainer)).willReturn(httpResponseContainer);
        given(mockEmulatorService.getLastApplicationRequest(TEST_REQUEST_NAME)).willReturn(httpRequestContainer);

    }

    @Test
    public void handleRequest() {
        emulatorController.handleRequest(httpServletRequest, httpServletResponse);

        then(mockRequestUtils).should().buildRequestContainer(httpServletRequest);
        then(mockEmulatorService).should().getResponse(httpRequestContainer);
        // assert response
        then(httpServletResponse).should(times(1)).setStatus(httpResponseContainer.getStatusCode());
        assertThat(httpResponseContainer.getHeaders().size(), is(not(0)));
        for(Map.Entry<String, List<String>> header : httpResponseContainer.getHeaders().entrySet()) {
            then(httpServletResponse).should(times(1)).addHeader(header.getKey(), header.getValue().get(0));
        }
        then(printWriter).should(times(1)).write(httpResponseContainer.getBody());
        then(printWriter).should(times(1)).flush();
    }

    @Test
    public void handleRequestOverrideHeaderSet() {
        String uriOverrideValue = "UriOverrideValue";
        given(httpServletRequest.getHeader(X_REQUEST_URI_OVERRIDE_HEADER_KEY)).willReturn(uriOverrideValue);

        emulatorController.handleRequest(httpServletRequest, httpServletResponse);

        then(mockRequestUtils).should().buildRequestContainer(httpServletRequest, uriOverrideValue);
        then(mockEmulatorService).should().getResponse(httpRequestContainer);
        // assert response
        then(httpServletResponse).should(times(1)).setStatus(httpResponseContainer.getStatusCode());
        then(httpServletResponse).should().addHeader(anyString(), anyString());
        for(Map.Entry<String, List<String>> header : httpResponseContainer.getHeaders().entrySet()) {
            then(httpServletResponse).should(times(1)).addHeader(header.getKey(), header.getValue().get(0));
        }
        then(printWriter).should(times(1)).write(httpResponseContainer.getBody());
        then(printWriter).should(times(1)).flush();
    }

    @Test
    public void handleRequestContentLengthHeaderSet() {
        httpResponseContainer.getHeaders().put(CONTENT_LENGTH_HEADER_KEY, Lists.newArrayList("555"));

        emulatorController.handleRequest(httpServletRequest, httpServletResponse);

        then(mockEmulatorService).should().getResponse(httpRequestContainer);

        // assert response
        then(httpServletResponse).should(times(1)).setStatus(httpResponseContainer.getStatusCode());
        then(httpServletResponse).should(times(0)).addHeader(CONTENT_LENGTH_HEADER_KEY, eq(anyString()));
        then(printWriter).should(times(1)).write(httpResponseContainer.getBody());
        then(printWriter).should(times(1)).flush();
    }

    @Test
    public void handleRequestIOException() throws IOException {
        given(httpServletResponse.getWriter()).willThrow(new IOException());

        thrown.expect(EmulatorException.class);
        thrown.expectMessage("There was an error writing the http response body");

        emulatorController.handleRequest(httpServletRequest, httpServletResponse);
    }

    @Test
    public void resetEmulator() {
        emulatorController.resetEmulator();

        then(mockEmulatorService).should().reset();
    }

    @Test
    public void getLastApplicationRequest() {
        HttpRequestContainer lastApplicationRequest = emulatorController.getLastApplicationRequest(TEST_REQUEST_NAME);

        then(mockEmulatorService).should().getLastApplicationRequest(TEST_REQUEST_NAME);
        assertThat(lastApplicationRequest, is(httpRequestContainer));
    }

    @Test
    public void getLastApplicationRequestNullRequestType() {
        thrown.expect(HttpClientErrorException.class);
        thrown.expectMessage("'requestType' URL param was not set");

        emulatorController.getLastApplicationRequest(null);
    }

    @Test
    public void getLastApplicationRequestEmptyRequestType() {
        thrown.expect(HttpClientErrorException.class);
        thrown.expectMessage("'requestType' URL param was not set");

        emulatorController.getLastApplicationRequest("");
    }

    @Test
    public void getLastApplicationRequestNotFound() {
        given(mockEmulatorService.getLastApplicationRequest(TEST_REQUEST_NAME)).willReturn(null);

        thrown.expect(ApplicationRequestNotFoundException.class);
        thrown.expectMessage(String.format("[%s] request was never sent to the emulator", TEST_REQUEST_NAME));

        emulatorController.getLastApplicationRequest(TEST_REQUEST_NAME);
    }

    @Test
    public void addResponseRule() {
        HttpResponseRule responseRule = new HttpResponseRule();
        emulatorController.addResponseRule(responseRule);

        then(mockEmulatorService).should().addResponseRule(responseRule);
    }

    @Test
    public void handlePCFRequest() {
        emulatorController.handlePCFRequest();
        // Method does nothing, therefore nothing to assert. Just for Code Coverage.
    }
}