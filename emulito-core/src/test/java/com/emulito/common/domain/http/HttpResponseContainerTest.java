package com.emulito.common.domain.http;

import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.util.List;
import java.util.Map;

import static com.google.common.collect.Lists.newArrayList;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.collection.IsMapContaining.hasEntry;
import static org.hamcrest.MatcherAssert.assertThat;

public class HttpResponseContainerTest {

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    public static final int TEST_STATUS_CODE = 333;
    public static final Map<String, List<String>> TEST_HEADERS = ImmutableMap.<String, List<String>>builder().
            put("firstHeaderKey", newArrayList("firstHeaderValue")).
            put("secondHeaderKey", newArrayList("secondHeaderValue"))
            .build();
    public static final String TEST_BODY = "testBody";

    private HttpResponseContainer httpResponseContainer;

    @Before
    public void setup() {
        httpResponseContainer = new HttpResponseContainer();
    }

    @Test
    public void setAndGetStatusCode() {
        httpResponseContainer.setStatusCode(TEST_STATUS_CODE);
        assertThat(httpResponseContainer.getStatusCode(), is(TEST_STATUS_CODE));
    }

    @Test
    public void setAndGetHeaders() {
        httpResponseContainer.setHeaders(TEST_HEADERS);
        assertThat(httpResponseContainer.getHeaders(), is(TEST_HEADERS));
    }

    @Test
    public void addHeader() {
        httpResponseContainer.setHeaders(TEST_HEADERS);
        httpResponseContainer.addHeader("newHeaderKey", "newHeaderValue");
        assertThat(httpResponseContainer.getHeaders(), hasEntry("firstHeaderKey", newArrayList("firstHeaderValue")));
        assertThat(httpResponseContainer.getHeaders(), hasEntry("secondHeaderKey", newArrayList("secondHeaderValue")));
        assertThat(httpResponseContainer.getHeaders(), hasEntry("newHeaderKey", newArrayList("newHeaderValue")));
    }

    @Test
    public void setAndGetBody() {
        httpResponseContainer.setBody(TEST_BODY);
        assertThat(httpResponseContainer.getBody(), is(TEST_BODY));
    }

    @Test
    public void toStringTest() {
        httpResponseContainer.setStatusCode(TEST_STATUS_CODE);
        httpResponseContainer.setHeaders(TEST_HEADERS);
        httpResponseContainer.setBody(TEST_BODY);
        assertThat(httpResponseContainer.toString(), is("{\"statusCode\":333,\"headers\":{firstHeaderKey=[firstHeaderValue], secondHeaderKey=[secondHeaderValue]},\"body\":\"testBody\"}"));
    }
}
