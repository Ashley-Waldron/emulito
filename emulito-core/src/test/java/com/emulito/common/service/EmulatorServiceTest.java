package com.emulito.common.service;

import com.emulito.common.domain.http.HttpRequestContainer;
import com.emulito.common.domain.http.HttpResponseContainer;
import com.emulito.common.domain.http.HttpResponseRule;
import com.emulito.common.domain.predicates.RequestPredicate;
import com.emulito.common.exception.EmulatorException;
import com.emulito.common.test.utils.TestUtils;
import org.hamcrest.MatcherAssert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.*;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Set;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.core.IsNot.not;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

public class EmulatorServiceTest {

    private static final String TEST_REQUEST_TYPE = "TestRequestType";

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Captor
    ArgumentCaptor<Set<RequestPredicate>> captor;

    @Mock
    private RequestResponseHandler requestResponseHandler;

    @Mock
    private PredicateBuilder predicateBuilder;

    @InjectMocks
    private EmulatorService emulatorService = new EmulatorService();

    private Set<RequestPredicate> requestPredicates;
    private HttpRequestContainer testHttpRequestContainer;
    private HttpResponseRule testResponseRule;

    @Before
    @SuppressWarnings("unchecked")
    public void setup() {
        requestPredicates = (Set<RequestPredicate>) ReflectionTestUtils.getField(emulatorService, "requestPredicates");

        testHttpRequestContainer = new HttpRequestContainer();
        given(requestResponseHandler.getLastApplicationRequest(TEST_REQUEST_TYPE)).willReturn(testHttpRequestContainer);

    }

    @Test
    public void reset() {
        TestUtils.TestRequestPredicate foreverPredicate = TestUtils.buildRequestPredicate().setTimeToLive(HttpResponseRule.LifeTimes.FOREVER);
        TestUtils.TestRequestPredicate untilRestPredicate = TestUtils.buildRequestPredicate().setTimeToLive(HttpResponseRule.LifeTimes.UNTIL_RESET);
        TestUtils.TestRequestPredicate singleUsePredicate = TestUtils.buildRequestPredicate().setTimeToLive(HttpResponseRule.LifeTimes.SINGLE_USE);
        requestPredicates.add(foreverPredicate);
        requestPredicates.add(untilRestPredicate);
        requestPredicates.add(singleUsePredicate);

        emulatorService.reset();

        // Then
        then(requestResponseHandler).should().reset(captor.capture());
        // Predicate with time to live of forever is not passed to the response handler
        MatcherAssert.assertThat(captor.getValue(), hasItem(singleUsePredicate));
        MatcherAssert.assertThat(captor.getValue(), hasItem(untilRestPredicate));
        MatcherAssert.assertThat(captor.getValue(), not(hasItem(foreverPredicate)));

        // All predicates except ones with time to live of forever are removed from predicate list
        MatcherAssert.assertThat(requestPredicates, not(hasItem(singleUsePredicate)));
        MatcherAssert.assertThat(requestPredicates, not(hasItem(untilRestPredicate)));
        MatcherAssert.assertThat(requestPredicates, hasItem(foreverPredicate));
    }

    @Test
    public void getLastApplicationRequest() {
        assertThat(emulatorService.getLastApplicationRequest(TEST_REQUEST_TYPE), is(testHttpRequestContainer));
    }

    @Test
    public void addResponseRule() {
        TestUtils.TestRequestPredicate testRequestPredicate = TestUtils.buildRequestPredicate();
        testResponseRule = new HttpResponseRule();
        given(predicateBuilder.buildPredicate(testResponseRule)).willReturn(testRequestPredicate);

        emulatorService.addResponseRule(testResponseRule);

        then(predicateBuilder).should().buildPredicate(testResponseRule);
        MatcherAssert.assertThat(requestPredicates, hasItem(testRequestPredicate));
        then(requestResponseHandler).should().setNextResponse(testRequestPredicate, testResponseRule);
    }

    @Test
    public void getResponseSingleUse() {
        TestUtils.TestRequestPredicate testRequestPredicate = TestUtils.buildRequestPredicate();
        testRequestPredicate.setTimeToLive(HttpResponseRule.LifeTimes.SINGLE_USE);
        testRequestPredicate.setRequestType(TEST_REQUEST_TYPE);
        testRequestPredicate.setResult(true);
        requestPredicates.add(testRequestPredicate);

        HttpResponseRule testHttpResponseRule = new HttpResponseRule();
        HttpResponseContainer testResponse = new HttpResponseContainer();
        testHttpResponseRule.setResponse(testResponse);
        given(requestResponseHandler.removeNextResponse(testRequestPredicate)).willReturn(testHttpResponseRule);

        HttpResponseContainer response = emulatorService.getResponse(testHttpRequestContainer);

        assertThat(response, is(testResponse));
        then(requestResponseHandler).should().removeNextResponse(testRequestPredicate);
        then(requestResponseHandler).should(times(0)).getNextResponse(testRequestPredicate);
        assertThat(requestPredicates.size(), is(0));
        then(requestResponseHandler).should().setLastApplicationRequest(testRequestPredicate.getRequestType(), testHttpRequestContainer);
    }

    @Test
    public void getResponseForever() {
        TestUtils.TestRequestPredicate testRequestPredicate = TestUtils.buildRequestPredicate();
        testRequestPredicate.setTimeToLive(HttpResponseRule.LifeTimes.FOREVER);
        testRequestPredicate.setRequestType(TEST_REQUEST_TYPE);
        testRequestPredicate.setResult(true);
        requestPredicates.add(testRequestPredicate);

        HttpResponseRule testHttpResponseRule = new HttpResponseRule();
        HttpResponseContainer testResponse = new HttpResponseContainer();
        testHttpResponseRule.setResponse(testResponse);
        given(requestResponseHandler.getNextResponse(testRequestPredicate)).willReturn(testHttpResponseRule);

        HttpResponseContainer response = emulatorService.getResponse(testHttpRequestContainer);

        assertThat(response, is(testResponse));
        then(requestResponseHandler).should().getNextResponse(testRequestPredicate);
        then(requestResponseHandler).should(times(0)).removeNextResponse(testRequestPredicate);
        MatcherAssert.assertThat(requestPredicates, hasItem(testRequestPredicate));
        then(requestResponseHandler).should().setLastApplicationRequest(testRequestPredicate.getRequestType(), testHttpRequestContainer);
    }

    @Test
    public void getResponseMultipleMatchesDifferentPriorities() {
        TestUtils.TestRequestPredicate testRequestPredicate1 = TestUtils.buildRequestPredicate();
        testRequestPredicate1.setTimeToLive(HttpResponseRule.LifeTimes.FOREVER);
        testRequestPredicate1.setRequestType(TEST_REQUEST_TYPE);
        testRequestPredicate1.setResult(true);
        testRequestPredicate1.setPriority(1);
        requestPredicates.add(testRequestPredicate1);

        TestUtils.TestRequestPredicate testRequestPredicate2 = TestUtils.buildRequestPredicate();
        testRequestPredicate2.setTimeToLive(HttpResponseRule.LifeTimes.FOREVER);
        testRequestPredicate2.setRequestType(TEST_REQUEST_TYPE);
        testRequestPredicate2.setResult(true);
        testRequestPredicate2.setPriority(2);
        requestPredicates.add(testRequestPredicate2);

        HttpResponseRule testHttpResponseRule = new HttpResponseRule();
        HttpResponseContainer testResponse = new HttpResponseContainer();
        testHttpResponseRule.setResponse(testResponse);

        given(requestResponseHandler.getNextResponse(testRequestPredicate2)).willReturn(testHttpResponseRule);

        HttpResponseContainer response = emulatorService.getResponse(testHttpRequestContainer);

        assertThat(response, is(testResponse));
        then(requestResponseHandler).should().getNextResponse(testRequestPredicate2);
        then(requestResponseHandler).should(times(0)).removeNextResponse(testRequestPredicate1);
        then(requestResponseHandler).should(times(0)).removeNextResponse(testRequestPredicate2);
        MatcherAssert.assertThat(requestPredicates, hasItems(testRequestPredicate1,testRequestPredicate1));
        then(requestResponseHandler).should().setLastApplicationRequest(testRequestPredicate2.getRequestType(), testHttpRequestContainer);
    }

    @Test
    public void getResponseNotFound() {
        // Given
        TestUtils.TestRequestPredicate testRequestPredicate = TestUtils.buildRequestPredicate();
        testRequestPredicate.setTimeToLive(HttpResponseRule.LifeTimes.FOREVER);
        testRequestPredicate.setRequestType(TEST_REQUEST_TYPE);
        testRequestPredicate.setResult(true);
        requestPredicates.add(testRequestPredicate);

        given(requestResponseHandler.getNextResponse(testRequestPredicate)).willReturn(null);

        // then
        thrown.expect(EmulatorException.class);
        thrown.expectMessage(String.format(
                "Received an unknown application request. Please ensure that there " +
                        "is a default response set up which will match the following request [%s]", testHttpRequestContainer));

        // When
        emulatorService.getResponse(testHttpRequestContainer);
    }

    @Test
    public void getResponseWithSleep() {
        TestUtils.TestRequestPredicate testRequestPredicate = TestUtils.buildRequestPredicate();
        testRequestPredicate.setTimeToLive(HttpResponseRule.LifeTimes.FOREVER);
        testRequestPredicate.setRequestType(TEST_REQUEST_TYPE);
        testRequestPredicate.setResult(true);
        requestPredicates.add(testRequestPredicate);

        HttpResponseRule testHttpResponseRule = new HttpResponseRule();
        testHttpResponseRule.setDelay(new HttpResponseRule.Delay().setDelayTimeInMillis(3000));
        HttpResponseContainer testResponse = new HttpResponseContainer();
        testHttpResponseRule.setResponse(testResponse);
        given(requestResponseHandler.getNextResponse(testRequestPredicate)).willReturn(testHttpResponseRule);

        long startTime = System.currentTimeMillis();
        HttpResponseContainer response = emulatorService.getResponse(testHttpRequestContainer);
        long stopTime = System.currentTimeMillis();

        assertThat(response, is(testResponse));
        assertThat(stopTime - startTime, is(greaterThan(2500L)));
        then(requestResponseHandler).should().getNextResponse(testRequestPredicate);
        then(requestResponseHandler).should(times(0)).removeNextResponse(testRequestPredicate);
        MatcherAssert.assertThat(requestPredicates, hasItem(testRequestPredicate));
        then(requestResponseHandler).should().setLastApplicationRequest(testRequestPredicate.getRequestType(), testHttpRequestContainer);
    }

    @Test
    public void getResponseWithNullSleep() {
        TestUtils.TestRequestPredicate testRequestPredicate = TestUtils.buildRequestPredicate();
        testRequestPredicate.setTimeToLive(HttpResponseRule.LifeTimes.FOREVER);
        testRequestPredicate.setRequestType(TEST_REQUEST_TYPE);
        testRequestPredicate.setResult(true);
        requestPredicates.add(testRequestPredicate);

        HttpResponseRule testHttpResponseRule = new HttpResponseRule();
        testHttpResponseRule.setDelay(null);
        HttpResponseContainer testResponse = new HttpResponseContainer();
        testHttpResponseRule.setResponse(testResponse);
        given(requestResponseHandler.getNextResponse(testRequestPredicate)).willReturn(testHttpResponseRule);

        long startTime = System.currentTimeMillis();
        HttpResponseContainer response = emulatorService.getResponse(testHttpRequestContainer);
        long stopTime = System.currentTimeMillis();

        assertThat(response, is(testResponse));
        assertThat(stopTime - startTime, is(not(greaterThan(1000L)))); // risky to include timing sensitive assertion in test
        then(requestResponseHandler).should().getNextResponse(testRequestPredicate);
        then(requestResponseHandler).should(times(0)).removeNextResponse(testRequestPredicate);
        MatcherAssert.assertThat(requestPredicates, hasItem(testRequestPredicate));
        then(requestResponseHandler).should().setLastApplicationRequest(testRequestPredicate.getRequestType(), testHttpRequestContainer);
    }

    @Test
    public void getResponseWithExceptionDuringSleep() {
        TestUtils.TestRequestPredicate testRequestPredicate = TestUtils.buildRequestPredicate();
        testRequestPredicate.setTimeToLive(HttpResponseRule.LifeTimes.FOREVER);
        testRequestPredicate.setRequestType(TEST_REQUEST_TYPE);
        testRequestPredicate.setResult(true);
        requestPredicates.add(testRequestPredicate);

        HttpResponseRule testHttpResponseRule = new HttpResponseRule();
        HttpResponseRule.Delay mockedDelayObject = Mockito.mock(HttpResponseRule.Delay.class);
        given(mockedDelayObject.getDelayTimeInMillis()).willReturn(1L).willReturn(1L).willThrow(new RuntimeException());
        testHttpResponseRule.setDelay(mockedDelayObject);
        HttpResponseContainer testResponse = new HttpResponseContainer();
        testHttpResponseRule.setResponse(testResponse);
        given(requestResponseHandler.getNextResponse(testRequestPredicate)).willReturn(testHttpResponseRule);

        // then
        thrown.expect(EmulatorException.class);
        thrown.expectMessage("The thread was interrupted while implementing a response wait time");

        emulatorService.getResponse(testHttpRequestContainer);
    }
}