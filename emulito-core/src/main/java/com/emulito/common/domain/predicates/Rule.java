package com.emulito.common.domain.predicates;

import com.emulito.common.domain.http.HttpRequestContainer;
import com.emulito.common.exception.EmulatorException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;

import static org.apache.commons.lang3.builder.ToStringStyle.JSON_STYLE;

/**
 * Created by Ashley Waldron (e062130) on 1/10/2019.
 */
public final class Rule {

    private static final String URL_PARAMS_CONTAIN_KEYWORD = "url parameters contain parameter";
    private static final String HEADERS_CONTAIN_KEYWORD = "headers contain entry";
    private static final String VALUE_ENCLOSING_CHARACTER = "'";

    public enum RuleTypes {
        HTTP_METHOD,
        URL,
        BODY,
        URL_PARAMETER,
        HEADER
    }

    private String ruleText;
    private String ruleMatchText;
    private String requiredValue;
    private RuleTypes ruleType;
    private String requiredKey;

    private Rule() {
    }

    public String getRuleText() {
        return ruleText;
    }

    public String getRuleMatchText() {
        return ruleMatchText;
    }

    public String getRequiredValue() {
        return requiredValue;
    }

    public RuleTypes getRuleType() {
        return ruleType;
    }

    public String getRequiredKey() {
        return requiredKey;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, JSON_STYLE);
    }

    public static final class RuleBuilder {

        public static Rule build(String requestRule) {
            Rule rule = new Rule();
            rule.ruleText = requestRule;
            rule.ruleMatchText = StringUtils.substringAfterLast(requestRule, " ");
            rule.requiredValue = sanitiseValue(rule.ruleMatchText);

            if (rule.ruleText.contains(HttpRequestContainer.URL_NODE_KEY) && !rule.ruleText.startsWith(URL_PARAMS_CONTAIN_KEYWORD)) {
                rule.ruleType = RuleTypes.URL;
            } else if (rule.ruleText.contains(HttpRequestContainer.BODY_NODE_KEY)) {
                rule.ruleType = RuleTypes.BODY;
            } else if (rule.ruleText.contains(HttpRequestContainer.HTTP_METHOD_NODE_KEY)) {
                rule.ruleType = RuleTypes.HTTP_METHOD;
            } else if (rule.ruleText.startsWith(URL_PARAMS_CONTAIN_KEYWORD)) {
                rule.ruleType = RuleTypes.URL_PARAMETER;
                rule.requiredKey = getRequiredKey(rule.ruleText);
            } else if (rule.ruleText.startsWith(HEADERS_CONTAIN_KEYWORD)) {
                rule.ruleType = RuleTypes.HEADER;
                rule.requiredKey = getRequiredKey(rule.ruleText);
            } else {
                throw new EmulatorException(String.format("Unknown Rule type specified for rule [%s]", rule.ruleText));
            }
            return rule;
        }

        private static String sanitiseValue(String ruleMatchText) {
            // strip off the surrounding enclosing characters
            String result;
            if(ruleMatchText.startsWith(VALUE_ENCLOSING_CHARACTER)
                    && ruleMatchText.endsWith(VALUE_ENCLOSING_CHARACTER)) {
                result = ruleMatchText.substring(1, ruleMatchText.length() - 1);
            } else {
                result = ruleMatchText;
            }
            return result;
        }

        private static String getRequiredKey(String ruleText) {
            String[] requiredKeys = ruleText.split(VALUE_ENCLOSING_CHARACTER);
            if(requiredKeys.length != 4) {
                throw new EmulatorException("Rule text was invalid: Expected key value pair for asserting");
            }
            return requiredKeys[1];
        }
    }
}