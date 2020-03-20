package com.emulito.common.domain.predicates;

import com.emulito.common.exception.EmulatorException;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

public class RuleTest {

    @org.junit.Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @org.junit.Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void createRuleForUrl() {
        String ruleText = "url contains 'some'";
        Rule rule = Rule.RuleBuilder.build(ruleText);
        assertThat(rule.getRuleType(), is(Rule.RuleTypes.URL));
        assertThat(rule.getRuleMatchText(), is("'some'"));
        assertThat(rule.getRuleText(), is(ruleText));
        assertThat(rule.getRequiredKey(), is(nullValue()));
        assertThat(rule.getRequiredValue(), is("some"));
    }

    @Test
    public void createRuleForBody() {
        String ruleText = "body contains 'some'";
        Rule rule = Rule.RuleBuilder.build(ruleText);
        assertThat(rule.getRuleType(), is(Rule.RuleTypes.BODY));
        assertThat(rule.getRuleMatchText(), is("'some'"));
        assertThat(rule.getRuleText(), is(ruleText));
        assertThat(rule.getRequiredKey(), is(nullValue()));
        assertThat(rule.getRequiredValue(), is("some"));
    }

    @Test
    public void createRuleForHttpMethod() {
        String ruleText = "httpMethod contains 'some'";
        Rule rule = Rule.RuleBuilder.build(ruleText);
        assertThat(rule.getRuleType(), is(Rule.RuleTypes.HTTP_METHOD));
        assertThat(rule.getRuleMatchText(), is("'some'"));
        assertThat(rule.getRuleText(), is(ruleText));
        assertThat(rule.getRequiredKey(), is(nullValue()));
        assertThat(rule.getRequiredValue(), is("some"));
    }

    @Test
    public void createRuleForUrlParameters() {
        String ruleText = "url parameters contain parameter 'some parameter name' whose value contains 'some'";
        Rule rule = Rule.RuleBuilder.build(ruleText);
        assertThat(rule.getRuleType(), is(Rule.RuleTypes.URL_PARAMETER));
        assertThat(rule.getRuleMatchText(), is("'some'"));
        assertThat(rule.getRuleText(), is(ruleText));
        assertThat(rule.getRequiredKey(), is("some parameter name"));
        assertThat(rule.getRequiredValue(), is("some"));
    }

    @Test
    public void createRuleForHeaders() {
        String ruleText = "headers contain entry 'some parameter name' whose value contains 'some'";
        Rule rule = Rule.RuleBuilder.build(ruleText);
        assertThat(rule.getRuleType(), is(Rule.RuleTypes.HEADER));
        assertThat(rule.getRuleMatchText(), is("'some'"));
        assertThat(rule.getRuleText(), is(ruleText));
        assertThat(rule.getRequiredKey(), is("some parameter name"));
        assertThat(rule.getRequiredValue(), is("some"));
    }

    @Test
    public void createRuleForHeadersWithNullRequiredValue() {
        String ruleText = "headers contain entry 'some parameter name'";

        thrown.expect(EmulatorException.class);
        thrown.expectMessage("Rule text was invalid: Expected key value pair for asserting");

        Rule rule = Rule.RuleBuilder.build(ruleText);
    }

    @Test
    public void createRuleInvalid() {
        // Given
        String ruleText = "invalid contains 'some'";

        // Then
        thrown.expect(EmulatorException.class);
        thrown.expectMessage("Unknown Rule type specified for rule [invalid contains 'some']");

        // When
        Rule.RuleBuilder.build(ruleText);
    }

    @Test
    public void sanitiseValue() {
        String ruleText = "url contains some";
        Rule rule = Rule.RuleBuilder.build(ruleText);
        assertThat(rule.getRequiredValue(), is("some"));
    }

    @Test
    public void toStringTest() {
        String ruleText = "url contains 'some'";
        Rule rule = Rule.RuleBuilder.build(ruleText);
        assertThat(rule.toString(), is("{\"ruleText\":\"url contains 'some'\",\"ruleMatchText\":\"'some'\",\"requiredValue\":\"some\",\"ruleType\":\"URL\",\"requiredKey\":null}"));
    }
}
