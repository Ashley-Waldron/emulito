package com.emulito.common.domain.predicates;

import com.emulito.common.domain.http.HttpRequestContainer;
import com.emulito.common.exception.EmulatorException;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.function.Predicate;

/**
 * Created by Ashley Waldron (e062130) on 1/9/2019.
 */
public final class HttpRequestPredicateFactory {

    private static final Logger LOG = LoggerFactory.getLogger(HttpRequestPredicateFactory.class);

    private static final String IS_KEYWORD = "is";
    private static final String CONTAINS_KEYWORD = "contains";
    private static final String ENDS_WITH_KEYWORD = "ends with";
    private static final String STARTS_WITH_KEYWORD = "starts with";
    private static final String MATCHES_KEYWORD = "matches";

    private HttpRequestPredicateFactory() {
    }

    public static Predicate<HttpRequestContainer> getPredicate(Rule rule) {
        switch (rule.getRuleType()) {
            case HTTP_METHOD:
                return getPredicate(rule, HttpRequestContainer::getHttpMethod);
            case URL:
                return getPredicate(rule, HttpRequestContainer::getUrl);
            case BODY:
                return getPredicate(rule, HttpRequestContainer::getBody);
            case URL_PARAMETER:
                return getPredicate(rule, x -> getFirst(x.getParams().get(rule.getRequiredKey())));
            case HEADER:
                return getPredicate(rule, x -> getFirst(x.getHeaders().get(rule.getRequiredKey())));
            default:
                throw new EmulatorException(String.format("Rule [%s] is not supported", rule));
        }
    }

    private static Predicate<HttpRequestContainer> getPredicate(Rule rule, ValueExtractor<HttpRequestContainer> valueExtractor) {
        if (rule.getRuleText().contains(IS_KEYWORD)) {
            LOG.debug("Adding '{}' predicate for rule [{}]", IS_KEYWORD, rule); // NOSONAR
            return Predicates.equalsPredicate(valueExtractor, rule.getRequiredValue());
        } else if (rule.getRuleText().contains(CONTAINS_KEYWORD)) {
            LOG.debug("Adding '{}' predicate for rule [{}]", CONTAINS_KEYWORD, rule);
            return Predicates.containsPredicate(valueExtractor, rule.getRequiredValue());
        } else if (rule.getRuleText().contains(ENDS_WITH_KEYWORD)) {
            LOG.debug("Adding '{}' predicate for rule [{}]", ENDS_WITH_KEYWORD, rule);
            return Predicates.endsWithPredicate(valueExtractor, rule.getRequiredValue());
        } else if (rule.getRuleText().contains(STARTS_WITH_KEYWORD)) {
            LOG.debug("Adding '{}' predicate for rule [{}]", STARTS_WITH_KEYWORD, rule);
            return Predicates.startsWithPredicate(valueExtractor, rule.getRequiredValue());
        } else if (rule.getRuleText().contains(MATCHES_KEYWORD)) {
            LOG.debug("Adding '{}' predicate for rule [{}]", MATCHES_KEYWORD, rule);
            return Predicates.matchesPredicate(valueExtractor, rule.getRequiredValue());
        } else {
            LOG.debug("Rule [{}] is not supported, ignoring....", rule);
            throw new EmulatorException(String.format("Rule [%s] is not supported, ignoring....", rule));
        }
    }

    private static String getFirst(List<String> list) {
        return CollectionUtils.isEmpty(list) ? null : list.get(0);
    }

}
