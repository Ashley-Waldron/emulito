package com.emulito.common.service;

import com.emulito.common.domain.http.HttpRequestContainer;
import com.emulito.common.domain.http.HttpResponseRule;
import com.emulito.common.domain.predicates.RequestPredicate;
import com.emulito.common.exception.EmulatorException;
import com.emulito.common.test.utils.TestUtils;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class PredicateBuilderTest {

    @Rule
    public MockitoRule mockitoJUnit = MockitoJUnit.rule();

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private PredicateBuilder predicateBuilder;

    @Before
    public void setup() {
        predicateBuilder = new PredicateBuilder();
    }

    @Test
    public void buildPredicate() {
        HttpResponseRule httpResponseRule = TestUtils.buildHttpResponseRule();
        RequestPredicate requestPredicate = predicateBuilder.buildPredicate(httpResponseRule);
        assertThat(requestPredicate.getRequestType(), is(httpResponseRule.getPredicate().getRequestType()));
    }

    @Test
    public void buildPredicateNoRules() {
        // Given
        HttpResponseRule httpResponseRule = TestUtils.buildHttpResponseRule();
        httpResponseRule.getPredicate().setRules(Collections.emptyList());

        // Then
        thrown.expect(EmulatorException.class);
        thrown.expectMessage(String.format("Predicate definition was not supplied for response rule [{\"predicate\":{\"requestType\":\"%s\",\"rules\":[]},\"priority\":%s,\"timeToLive\":\"%s\",\"delay\":{\"delayTimeInMillis\":%s},\"response\":{\"statusCode\":%s,\"headers\":%s,\"body\":\"%s\"}}]",
                httpResponseRule.getPredicate().getRequestType(), httpResponseRule.getPriority(), httpResponseRule.getTimeToLive(),
                httpResponseRule.getDelay().getDelayTimeInMillis(), httpResponseRule.getResponse().getStatusCode(),
                httpResponseRule.getResponse().getHeaders(), httpResponseRule.getResponse().getBody()));

        // When
        predicateBuilder.buildPredicate(httpResponseRule);
    }

    @Test
    public void buildPredicateEmptyRequestType() {
        // Given
        HttpResponseRule httpResponseRule = TestUtils.buildHttpResponseRule();
        httpResponseRule.getPredicate().setRequestType("");

        // Then
        thrown.expect(EmulatorException.class);
        thrown.expectMessage(String.format("Predicate request type was not supplied for response rule [{\"predicate\":{\"requestType\":\"%s\",\"rules\":%s},\"priority\":%s,\"timeToLive\":\"%s\",\"delay\":{\"delayTimeInMillis\":%s},\"response\":{\"statusCode\":%s,\"headers\":%s,\"body\":\"%s\"}}]",
                httpResponseRule.getPredicate().getRequestType(), httpResponseRule.getPredicate().getRules(), httpResponseRule.getPriority(), httpResponseRule.getTimeToLive(),
                httpResponseRule.getDelay().getDelayTimeInMillis(), httpResponseRule.getResponse().getStatusCode(),
                httpResponseRule.getResponse().getHeaders(), httpResponseRule.getResponse().getBody()));

        // When
        predicateBuilder.buildPredicate(httpResponseRule);
    }

    @Test
    public void getTimeToLive() {
        HttpResponseRule httpResponseRule = TestUtils.buildHttpResponseRule();
        RequestPredicate requestPredicate = predicateBuilder.buildPredicate(httpResponseRule);
        assertThat(requestPredicate.getTimeToLive(), is(httpResponseRule.getTimeToLive()));
    }

    @Test
    public void getPriority() {
        HttpResponseRule httpResponseRule = TestUtils.buildHttpResponseRule();
        RequestPredicate requestPredicate = predicateBuilder.buildPredicate(httpResponseRule);
        assertThat(requestPredicate.getPriority(), is(httpResponseRule.getPriority()));
    }

    @Test
    public void testTrue() {
        HttpResponseRule httpResponseRule = TestUtils.buildHttpResponseRule();
        RequestPredicate requestPredicate = predicateBuilder.buildPredicate(httpResponseRule);
        HttpRequestContainer testHttpRequestContainer = new HttpRequestContainer();
        testHttpRequestContainer.setUrl("someValue");
        testHttpRequestContainer.setBody("someValue");
        assertThat(requestPredicate.test(testHttpRequestContainer), is(true));
    }

    @Test
    public void testFalse() {
        HttpResponseRule httpResponseRule = TestUtils.buildHttpResponseRule();
        RequestPredicate requestPredicate = predicateBuilder.buildPredicate(httpResponseRule);
        HttpRequestContainer testHttpRequestContainer = new HttpRequestContainer();
        testHttpRequestContainer.setUrl("someValue");
        testHttpRequestContainer.setBody("someOtherValue");
        assertThat(requestPredicate.test(testHttpRequestContainer), is(false));
    }

    @Test
    @Ignore
    public void hashCodeTest() {
        HttpResponseRule httpResponseRule = TestUtils.buildHttpResponseRule();
        RequestPredicate requestPredicate = predicateBuilder.buildPredicate(httpResponseRule);
        RequestPredicate requestPredicate2 = predicateBuilder.buildPredicate(httpResponseRule);

        assertThat(requestPredicate.hashCode(), is(equalTo(requestPredicate2.hashCode())));
    }

    @Test
    @Ignore
    public void equalsTest() {
        HttpResponseRule httpResponseRule = TestUtils.buildHttpResponseRule();
        RequestPredicate requestPredicate = predicateBuilder.buildPredicate(httpResponseRule);
        RequestPredicate requestPredicate2 = predicateBuilder.buildPredicate(httpResponseRule);

        assertThat(requestPredicate.equals(requestPredicate2), is(true));
    }
}
