package com.emulito.common.service;

import com.google.common.collect.Sets;
import com.emulito.common.domain.http.HttpRequestContainer;
import com.emulito.common.domain.http.HttpResponseRule;
import com.emulito.common.domain.predicates.RequestPredicate;
import com.emulito.common.test.utils.TestUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

public class RequestResponseHandlerTest {

    private static final String TEST_REQUEST_NAME = "TestRequestName";
    private static final int MAX_REQUESTS = 100;

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    private RequestResponseHandler requestResponseHandler = new RequestResponseHandler();

    @Before
    public void setup() {
        ReflectionTestUtils.setField(requestResponseHandler, "nextResponses", new HashMap<>());
        ReflectionTestUtils.setField(requestResponseHandler, "applicationRequests", new ConcurrentHashMap<>());
    }

    @Test
    public void setAndGetLastApplicationRequestDoesntExist() {
        assertThat(requestResponseHandler.getLastApplicationRequest(TEST_REQUEST_NAME), is(nullValue()));
    }

    @Test
    public void setAndGetLastApplicationRequest() {
        // Given
        HttpRequestContainer httpRequestContainer = new HttpRequestContainer();
        requestResponseHandler.setLastApplicationRequest(TEST_REQUEST_NAME, httpRequestContainer);

        // When / Then
        assertThat(requestResponseHandler.getLastApplicationRequest(TEST_REQUEST_NAME), is(httpRequestContainer));
    }

    @Test
    public void setAndGetLastApplicationRequestMoreThanOne() {
        // Given
        HttpRequestContainer httpRequestContainer = new HttpRequestContainer();
        requestResponseHandler.setLastApplicationRequest(TEST_REQUEST_NAME, httpRequestContainer);
        HttpRequestContainer httpRequestContainer2 = new HttpRequestContainer();
        requestResponseHandler.setLastApplicationRequest(TEST_REQUEST_NAME, httpRequestContainer2);

        // When / Then
        // Requests are returned on the order of last in first out
        assertThat(requestResponseHandler.getLastApplicationRequest(TEST_REQUEST_NAME), is(httpRequestContainer2));
        assertThat(requestResponseHandler.getLastApplicationRequest(TEST_REQUEST_NAME), is(httpRequestContainer));

        // After all requests of that type are retrieved no more should be returned
        assertThat(requestResponseHandler.getLastApplicationRequest(TEST_REQUEST_NAME), is(nullValue()));
    }

    @Test
    public void setAndGetLastApplicationRequestGreaterThanMax() {
        // Given
        // Set the very first request
        HttpRequestContainer firstHttpRequestContainer = new HttpRequestContainer();
        requestResponseHandler.setLastApplicationRequest(TEST_REQUEST_NAME, firstHttpRequestContainer);
        // Fill up the rest of the request storage with more requests
        for (int count = 0; count < MAX_REQUESTS - 1; count++) {
            requestResponseHandler.setLastApplicationRequest(TEST_REQUEST_NAME, new HttpRequestContainer());
        }

        // When
        // Add one more request to overflow the storage
        HttpRequestContainer lastHttpRequestContainer = new HttpRequestContainer();
        requestResponseHandler.setLastApplicationRequest(TEST_REQUEST_NAME, lastHttpRequestContainer);

        // Then
        // First request returned should be the last one sent (as usual)
        assertThat(requestResponseHandler.getLastApplicationRequest(TEST_REQUEST_NAME), is(lastHttpRequestContainer));

        // retrieve all remaining requests except one
        for (int count = 0; count < MAX_REQUESTS - 2; count++) {
            requestResponseHandler.getLastApplicationRequest(TEST_REQUEST_NAME);
        }

        // Assert that the final request retrieved is not equal to the very first one set at the start of the test
        // proving that that one was purged from storage
        HttpRequestContainer finalApplicationRequestLeft = requestResponseHandler.getLastApplicationRequest(TEST_REQUEST_NAME);
        assertThat(finalApplicationRequestLeft, is(not(firstHttpRequestContainer)));
        assertThat(finalApplicationRequestLeft, is(not(nullValue())));

        // Finally verify there are no more requests left to retrieve
        assertThat(requestResponseHandler.getLastApplicationRequest(TEST_REQUEST_NAME), is(nullValue()));
    }

    @Test
    public void setAndGetNextResponse() {
        // Given
        RequestPredicate testRequestPredicate = TestUtils.buildRequestPredicate();
        HttpResponseRule testHttpResponseRule = new HttpResponseRule();

        // When
        requestResponseHandler.setNextResponse(testRequestPredicate, testHttpResponseRule);

        // Then
        assertThat(requestResponseHandler.getNextResponse(testRequestPredicate), is(testHttpResponseRule));
    }

    @Test
    public void setAndGetNextResponseRequestPredicateDoesntMatch() {
        // Given setup a response for a request predicate
        requestResponseHandler.setNextResponse(TestUtils.buildRequestPredicate(), new HttpResponseRule());

        // Attempt to get next response with a different request predicate
        assertThat(requestResponseHandler.getNextResponse(TestUtils.buildRequestPredicate()), is(nullValue()));
    }

    @Test
    public void removeNextResponse() {
        // Given
        RequestPredicate testRequestPredicate = TestUtils.buildRequestPredicate();
        requestResponseHandler.setNextResponse(testRequestPredicate, new HttpResponseRule());
        
        // When
        requestResponseHandler.removeNextResponse(testRequestPredicate);

        // Then
        assertThat(requestResponseHandler.getNextResponse(testRequestPredicate), is(nullValue()));
    }

    @Test
    public void reset() {
        // Given
        // setup a couple of requests for a request type
        String testRequestName1 = "TestRequestName1";
        requestResponseHandler.setLastApplicationRequest(testRequestName1, new HttpRequestContainer());
        requestResponseHandler.setLastApplicationRequest(testRequestName1, new HttpRequestContainer());

        // setup a couple of requests for another request type
        String testRequestName2 = "TestRequestName2";
        requestResponseHandler.setLastApplicationRequest(testRequestName2, new HttpRequestContainer());
        requestResponseHandler.setLastApplicationRequest(testRequestName2, new HttpRequestContainer());

        // Setup a couple of response rules
        RequestPredicate requestPredicate1 = TestUtils.buildRequestPredicate();
        RequestPredicate requestPredicate2 = TestUtils.buildRequestPredicate();
        requestResponseHandler.setNextResponse(requestPredicate1, new HttpResponseRule());
        requestResponseHandler.setNextResponse(requestPredicate2, new HttpResponseRule());

        // When
        requestResponseHandler.reset(Sets.newHashSet(requestPredicate1, requestPredicate2));

        // Then
        assertThat(requestResponseHandler.getLastApplicationRequest(testRequestName1), is(nullValue()));
        assertThat(requestResponseHandler.getLastApplicationRequest(testRequestName2), is(nullValue()));

        assertThat(requestResponseHandler.getNextResponse(requestPredicate1), is(nullValue()));
        assertThat(requestResponseHandler.getNextResponse(requestPredicate1), is(nullValue()));
    }


}
