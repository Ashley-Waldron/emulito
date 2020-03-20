package com.emulito.common.domain.http;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class HttpRequestContainerTest {

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    private static final String TEST_URL = "testUrl";
    private static final String TEST_HTTP_METHOD = "testHttpMethod";
    private static final Map<String, List<String>> TEST_PARAMS = ImmutableMap.<String, List<String>>builder().
            put("firstUrlParameterKey", Lists.newArrayList("firstUrlParameterValue")).
            put("secondUrlParameterKey", Lists.newArrayList("secondUrlParameterValue"))
            .build();
    private static final Map<String, List<String>> TEST_HEADERS = ImmutableMap.<String, List<String>>builder().
            put("firstHeaderKey", Lists.newArrayList("firstHeaderValue")).
            put("secondHeaderKey", Lists.newArrayList("secondHeaderValue"))
            .build();
    private static final String TEST_BODY = "testBody";

    private HttpRequestContainer httpRequestContainer;

    @Before
    public void setup() {
        httpRequestContainer = new HttpRequestContainer();
    }

    @Test
    public void setAndGetUrl() {
        httpRequestContainer.setUrl(TEST_URL);
        assertThat(httpRequestContainer.getUrl(), is(TEST_URL));
    }

    @Test
    public void setAndGetHttpMethod() {
        httpRequestContainer.setHttpMethod(TEST_HTTP_METHOD);
        assertThat(httpRequestContainer.getHttpMethod(), is(TEST_HTTP_METHOD));
    }

    @Test
    public void setAndGetUrlParameters() {
        httpRequestContainer.setParams(TEST_PARAMS);
        assertThat(httpRequestContainer.getParams(), is(TEST_PARAMS));
    }

    @Test
    public void setAndGetHeaders() {
        httpRequestContainer.setHeaders(TEST_HEADERS);
        assertThat(httpRequestContainer.getHeaders(), is(TEST_HEADERS));
    }

    @Test
    public void setAndGetBody() {
        httpRequestContainer.setBody(TEST_BODY);
        assertThat(httpRequestContainer.getBody(), is(TEST_BODY));
    }

    @Test
    public void toStringTest() {
        httpRequestContainer.setUrl(TEST_URL);
        httpRequestContainer.setHttpMethod(TEST_HTTP_METHOD);
        httpRequestContainer.setParams(TEST_PARAMS);
        httpRequestContainer.setHeaders(TEST_HEADERS);
        httpRequestContainer.setBody(TEST_BODY);
        assertThat(httpRequestContainer.toString(), is("{\"httpMethod\":\"testHttpMethod\",\"url\":\"testUrl\",\"params\":{firstUrlParameterKey=[firstUrlParameterValue], secondUrlParameterKey=[secondUrlParameterValue]},\"headers\":{firstHeaderKey=[firstHeaderValue], secondHeaderKey=[secondHeaderValue]},\"body\":\"testBody\"}"));
    }
}
