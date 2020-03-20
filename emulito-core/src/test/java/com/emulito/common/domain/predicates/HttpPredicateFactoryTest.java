package com.emulito.common.domain.predicates;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.emulito.common.domain.http.HttpRequestContainer;
import com.emulito.common.exception.EmulatorException;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class HttpPredicateFactoryTest {

    @org.junit.Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @org.junit.Rule
    public ExpectedException thrown = ExpectedException.none();

    private HttpRequestContainer testHttpRequestContainer;

    @Test
    public void getPredicateHttpMethodContains() {
        Rule testRule = Rule.RuleBuilder.build("httpMethod contains 'some'");
        Predicate<HttpRequestContainer> predicate = HttpRequestPredicateFactory.getPredicate(testRule);
        testHttpRequestContainer = new HttpRequestContainer();
        testHttpRequestContainer.setHttpMethod("someText");
        assertThat(predicate.test(testHttpRequestContainer), is(true));
    }

    @Test
    public void getPredicateUrlContains() {
        Rule testRule = Rule.RuleBuilder.build("url contains 'some'");
        Predicate<HttpRequestContainer> predicate = HttpRequestPredicateFactory.getPredicate(testRule);
        testHttpRequestContainer = new HttpRequestContainer();
        testHttpRequestContainer.setUrl("someText");
        assertThat(predicate.test(testHttpRequestContainer), is(true));
    }

    @Test
    public void getPredicateBodyContains() {
        Rule testRule = Rule.RuleBuilder.build("body contains 'some'");
        Predicate<HttpRequestContainer> predicate = HttpRequestPredicateFactory.getPredicate(testRule);
        testHttpRequestContainer = new HttpRequestContainer();
        testHttpRequestContainer.setBody("someText");
        assertThat(predicate.test(testHttpRequestContainer), is(true));
    }

    @Test
    public void getPredicateUrlParameterContains() {
        Rule testRule = Rule.RuleBuilder.build("url parameters contain parameter 'some parameter name' whose value contains 'some'");
        Predicate<HttpRequestContainer> predicate = HttpRequestPredicateFactory.getPredicate(testRule);
        testHttpRequestContainer = new HttpRequestContainer();
        Map<String, List<String>> urlParameterMap = ImmutableMap.<String, List<String>>builder().put("some parameter name", Lists.newArrayList("someText")).build();
        testHttpRequestContainer.setParams(urlParameterMap);
        assertThat(predicate.test(testHttpRequestContainer), is(true));
    }

    @Test
    public void getPredicateHeadersHasEntryWhoseValueContains() {
        Rule testRule = Rule.RuleBuilder.build("headers contain entry 'some parameter name' whose value contains 'some'");
        Predicate<HttpRequestContainer> predicate = HttpRequestPredicateFactory.getPredicate(testRule);
        testHttpRequestContainer = new HttpRequestContainer();
        Map<String, List<String>> headersMap = ImmutableMap.<String, List<String>>builder().put("some parameter name", Lists.newArrayList("someText")).build();
        testHttpRequestContainer.setHeaders(headersMap);
        assertThat(predicate.test(testHttpRequestContainer), is(true));
    }

    @Test
    public void getPredicateHeadersHasEntryWhoseValueIs() {
        Rule testRule = Rule.RuleBuilder.build("headers contain entry 'some parameter name' whose value is 'someText'");
        Predicate<HttpRequestContainer> predicate = HttpRequestPredicateFactory.getPredicate(testRule);
        testHttpRequestContainer = new HttpRequestContainer();
        Map<String, List<String>> headersMap = ImmutableMap.<String, List<String>>builder().put("some parameter name", Lists.newArrayList("someText")).build();
        testHttpRequestContainer.setHeaders(headersMap);
        assertThat(predicate.test(testHttpRequestContainer), is(true));
    }

    @Test
    public void getPredicateHeadersHasEntryWhoseValueStartsWith() {
        Rule testRule = Rule.RuleBuilder.build("headers contain entry 'some parameter name' whose value starts with 'some'");
        Predicate<HttpRequestContainer> predicate = HttpRequestPredicateFactory.getPredicate(testRule);
        testHttpRequestContainer = new HttpRequestContainer();
        Map<String, List<String>> headersMap = ImmutableMap.<String, List<String>>builder().put("some parameter name", Lists.newArrayList("someText")).build();
        testHttpRequestContainer.setHeaders(headersMap);
        assertThat(predicate.test(testHttpRequestContainer), is(true));
    }

    @Test
    public void getPredicateHeadersHasEntryWhoseValueEndsWith() {
        Rule testRule = Rule.RuleBuilder.build("headers contain entry 'some parameter name' whose value ends with 'Text'");
        Predicate<HttpRequestContainer> predicate = HttpRequestPredicateFactory.getPredicate(testRule);
        testHttpRequestContainer = new HttpRequestContainer();
        Map<String, List<String>> headersMap = ImmutableMap.<String, List<String>>builder().put("some parameter name", Lists.newArrayList("someText")).build();
        testHttpRequestContainer.setHeaders(headersMap);
        assertThat(predicate.test(testHttpRequestContainer), is(true));
    }

    @Test
    public void getPredicateHeadersHasEntryWhoseValueMatches() {
        Rule testRule = Rule.RuleBuilder.build("headers contain entry 'some parameter name' whose value matches '([A-Za-z]*)'");
        Predicate<HttpRequestContainer> predicate = HttpRequestPredicateFactory.getPredicate(testRule);
        testHttpRequestContainer = new HttpRequestContainer();
        Map<String, List<String>> headersMap = ImmutableMap.<String, List<String>>builder().put("some parameter name", Lists.newArrayList("someText")).build();
        testHttpRequestContainer.setHeaders(headersMap);
        assertThat(predicate.test(testHttpRequestContainer), is(true));
    }

    @Test
    @Ignore
    public void getPredicateRuleTypeNotRecognized() {
        // TODO not currently possible - investigate refactor
    }

    @Test
    public void getPredicateRuleTextNotRecognized() {
        // Given
        Rule testRule = Rule.RuleBuilder.build("url contains 'some'");
        ReflectionTestUtils.setField(testRule, "ruleText", "invalid");

        // Then
        thrown.expect(EmulatorException.class);
        thrown.expectMessage("Rule [{\"ruleText\":\"invalid\",\"ruleMatchText\":\"'some'\",\"requiredValue\":\"some\",\"ruleType\":\"URL\",\"requiredKey\":null}] is not supported, ignoring....");

        // When
        HttpRequestPredicateFactory.getPredicate(testRule);
    }
}
