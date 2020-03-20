package com.emulito.common.test.utils;

import com.google.common.collect.ImmutableList;
import com.emulito.common.domain.http.HttpRequestContainer;
import com.emulito.common.domain.http.HttpResponseContainer;
import com.emulito.common.domain.http.HttpResponseRule;
import com.emulito.common.domain.predicates.RequestPredicate;

import java.util.List;

import static com.emulito.common.domain.http.HttpResponseContainerTest.*;

public class TestUtils {

    private static final long TEST_DELAY_TIME_IN_MILLIS = 4000L;
    private static final String TEST_REQUEST_TYPE = "TestRequestType";
    private static final HttpResponseRule.LifeTimes TEST_TIME_TO_LIVE = HttpResponseRule.LifeTimes.SINGLE_USE;
    private static final int TEST_PRIORITY = 4;
    private static final List<String> TEST_RULES = ImmutableList.<String>builder().add("url contains 'someValue'").add("body contains 'someValue'").build();

    public static TestRequestPredicate buildRequestPredicate() {
        return new TestRequestPredicate();
    }

    public static class TestRequestPredicate implements RequestPredicate {
        List<String> rules;
        HttpResponseRule.LifeTimes timeToLive;
        String requestType;
        int priority;
        boolean result;

        public TestRequestPredicate setRules(List<String> rules) {
            this.rules = rules;
            return this;
        }

        public TestRequestPredicate setTimeToLive(HttpResponseRule.LifeTimes timeToLive) {
            this.timeToLive = timeToLive;
            return this;
        }

        public TestRequestPredicate setRequestType(String requestType) {
            this.requestType = requestType;
            return this;
        }

        public TestRequestPredicate setPriority(int priority) {
            this.priority = priority;
            return this;
        }

        public TestRequestPredicate setResult(boolean result) {
            this.result = result;
            return this;
        }

        @Override
        public List<String> getRules() {
            return rules;
        }

        @Override
        public HttpResponseRule.LifeTimes getTimeToLive() {
            return timeToLive;
        }

        @Override
        public String getRequestType() {
            return requestType;
        }

        @Override
        public int getPriority() {
            return priority;
        }

        @Override
        public boolean test(HttpRequestContainer httpRequestContainer) {
            return result;
        }
    }

    public static HttpResponseRule buildHttpResponseRule() {
        HttpResponseContainer testResponseContainer = new HttpResponseContainer();
        testResponseContainer.setStatusCode(TEST_STATUS_CODE);
        testResponseContainer.setHeaders(TEST_HEADERS);
        testResponseContainer.setBody(TEST_BODY);

        HttpResponseRule httpResponseRule = new HttpResponseRule();
        httpResponseRule.setResponse(testResponseContainer);

        httpResponseRule.setPriority(TEST_PRIORITY);
        httpResponseRule.setTimeToLive(TEST_TIME_TO_LIVE);

        HttpResponseRule.CustomRequestPredicateDefinition customRequestPredicateDefinition = new HttpResponseRule.CustomRequestPredicateDefinition();
        customRequestPredicateDefinition.setRequestType(TEST_REQUEST_TYPE);
        customRequestPredicateDefinition.setRules(TEST_RULES);
        httpResponseRule.setPredicate(customRequestPredicateDefinition);

        HttpResponseRule.Delay delay = new HttpResponseRule.Delay();
        delay.setDelayTimeInMillis(TEST_DELAY_TIME_IN_MILLIS);
        httpResponseRule.setDelay(delay);
        return httpResponseRule;
    }
}
