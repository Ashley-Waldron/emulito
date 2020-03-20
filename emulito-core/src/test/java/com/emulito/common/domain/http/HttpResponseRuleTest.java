package com.emulito.common.domain.http;

import com.google.common.collect.ImmutableList;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.util.List;

import static com.emulito.common.domain.http.HttpResponseContainerTest.*;
import static java.lang.String.format;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class HttpResponseRuleTest {

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    private static final long TEST_DELAY_TIME_IN_MILLIS = 4000L;
    private static final String TEST_REQUEST_TYPE = "TestRequestType";
    private static final HttpResponseRule.LifeTimes TEST_TIME_TO_LIVE = HttpResponseRule.LifeTimes.SINGLE_USE;
    private static final int TEST_PRIORITY = 4;
    private static final List<String> TEST_RULES = ImmutableList.<String>builder().add("TestRule1").add("TestRule2").build();

    private HttpResponseRule httpResponseRule;
    private HttpResponseRule.CustomRequestPredicateDefinition customRequestPredicateDefinition;
    private HttpResponseRule.Delay delay;

    @Before
    public void setup() {
        httpResponseRule = new HttpResponseRule();
        customRequestPredicateDefinition = new HttpResponseRule.CustomRequestPredicateDefinition();
        delay = new HttpResponseRule.Delay();
    }

    @Test
    public void setAndGetHttpResponseRulePredicate() {
        HttpResponseRule.CustomRequestPredicateDefinition testRequestPredicate = new HttpResponseRule.CustomRequestPredicateDefinition();
        httpResponseRule.setPredicate(testRequestPredicate);
        assertThat(httpResponseRule.getPredicate(), is(testRequestPredicate));
    }

    @Test
    public void setAndGetHttpResponseRuleResponse() {
        HttpResponseContainer testResponseContainer = new HttpResponseContainer();
        httpResponseRule.setResponse(testResponseContainer);
        assertThat(httpResponseRule.getResponse(), is(testResponseContainer));
    }

    @Test
    public void setAndGetHttpResponseRulePriority() {
        httpResponseRule.setPriority(TEST_PRIORITY);
        assertThat(httpResponseRule.getPriority(), is(TEST_PRIORITY));
    }

    @Test
    public void setAndGetHttpResponseRuleDelay() {
        HttpResponseRule.Delay testDelay = new HttpResponseRule.Delay();
        httpResponseRule.setDelay(testDelay);
        assertThat(httpResponseRule.getDelay(), is(testDelay));
    }

    @Test
    public void setAndGetHttpResponseRuleTimeToLive() {
        httpResponseRule.setTimeToLive(TEST_TIME_TO_LIVE);
        assertThat(httpResponseRule.getTimeToLive(), is(TEST_TIME_TO_LIVE));
    }

    @Test
    public void httpResponseRuleToString() {
        HttpResponseContainer testResponseContainer = new HttpResponseContainer();
        testResponseContainer.setStatusCode(TEST_STATUS_CODE);
        testResponseContainer.setHeaders(TEST_HEADERS);
        testResponseContainer.setBody(TEST_BODY);
        httpResponseRule.setResponse(testResponseContainer);

        httpResponseRule.setPriority(TEST_PRIORITY);
        httpResponseRule.setTimeToLive(TEST_TIME_TO_LIVE);

        customRequestPredicateDefinition.setRequestType(TEST_REQUEST_TYPE);
        customRequestPredicateDefinition.setRules(TEST_RULES);
        httpResponseRule.setPredicate(customRequestPredicateDefinition);

        delay.setDelayTimeInMillis(TEST_DELAY_TIME_IN_MILLIS);
        httpResponseRule.setDelay(delay);

        assertThat(httpResponseRule.toString(), is(format("{\"predicate\":{\"requestType\":\"%s\",\"rules\":[TestRule1, TestRule2]},\"priority\":%s,\"timeToLive\":\"%s\",\"delay\":{\"delayTimeInMillis\":%s},\"response\":{\"statusCode\":%s,\"headers\":{firstHeaderKey=[firstHeaderValue], secondHeaderKey=[secondHeaderValue]},\"body\":\"%s\"}}", TEST_REQUEST_TYPE, TEST_PRIORITY, TEST_TIME_TO_LIVE, TEST_DELAY_TIME_IN_MILLIS, TEST_STATUS_CODE, TEST_BODY)));
    }

    @Test
    public void setAndGetCustomRequestPredicateDefinitionRequestType() {
        customRequestPredicateDefinition.setRequestType(TEST_REQUEST_TYPE);
        assertThat(customRequestPredicateDefinition.getRequestType(), is(TEST_REQUEST_TYPE));
    }

    @Test
    public void setAndGetCustomRequestPredicateDefinitionRules() {
        customRequestPredicateDefinition.setRules(TEST_RULES);
        assertThat(customRequestPredicateDefinition.getRules(), is(TEST_RULES));
    }

    @Test
    public void customRequestPredicateDefinitionToString() {
        customRequestPredicateDefinition.setRequestType(TEST_REQUEST_TYPE);
        customRequestPredicateDefinition.setRules(TEST_RULES);
        assertThat(customRequestPredicateDefinition.toString(), is(format("{\"requestType\":\"%s\",\"rules\":[TestRule1, TestRule2]}", TEST_REQUEST_TYPE)));
    }

    @Test
    public void setAndGetDelay() {
        delay.setDelayTimeInMillis(TEST_DELAY_TIME_IN_MILLIS);
        assertThat(delay.getDelayTimeInMillis(), is(TEST_DELAY_TIME_IN_MILLIS));
    }

    @Test
    public void delayToString() {
        delay.setDelayTimeInMillis(TEST_DELAY_TIME_IN_MILLIS);
        assertThat(delay.toString(), is(format("{\"delayTimeInMillis\":%s}", TEST_DELAY_TIME_IN_MILLIS)));
    }
}
