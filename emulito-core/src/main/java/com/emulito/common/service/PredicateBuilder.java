package com.emulito.common.service;

import com.emulito.common.domain.predicates.RequestPredicate;
import com.emulito.common.domain.predicates.Rule;
import com.emulito.common.exception.EmulatorException;
import com.emulito.common.domain.http.HttpRequestContainer;
import com.emulito.common.domain.http.HttpResponseRule;
import com.emulito.common.domain.predicates.HttpRequestPredicateFactory;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Predicate;

/**
 * Created by Ashley Waldron (e062130) on 12/17/2018.
 */
@Component
public class PredicateBuilder {

    private static final Logger LOG = LoggerFactory.getLogger(PredicateBuilder.class);

    public RequestPredicate buildPredicate(HttpResponseRule responseRule) {
        return new DefaultRequestPredicate(responseRule);
    }

    private class DefaultRequestPredicate implements RequestPredicate {
        private final HttpResponseRule.CustomRequestPredicateDefinition predicateDefinition;
        private final int priority;
        private final HttpResponseRule.LifeTimes timeToLive;
        private Predicate<HttpRequestContainer> predicate;

        private DefaultRequestPredicate(HttpResponseRule responseRule) {
            LOG.debug("Building Dynamic Custom Request Predicate for response rule:\n[{}]", responseRule);
            validateResponseContainer(responseRule);
            this.predicateDefinition = responseRule.getPredicate();
            this.priority = responseRule.getPriority() < 1 ? 1 : responseRule.getPriority();
            this.timeToLive = responseRule.getTimeToLive();

            for (String requestRule : getRules()) {
                Rule rule = Rule.RuleBuilder.build(requestRule);
                addAndPredicate(HttpRequestPredicateFactory.getPredicate(rule));
            }
            if (predicate == null) {
                throw new EmulatorException("Custom predicate is not of the correct format");
            }
        }

        private void validateResponseContainer(HttpResponseRule responseRule) {
            if (responseRule == null
                    || CollectionUtils.isEmpty(responseRule.getPredicate().getRules())) {
                throw new EmulatorException(
                        String.format("Predicate definition was not supplied for response rule [%s]", responseRule));
            } else if(StringUtils.isEmpty(responseRule.getPredicate().getRequestType())) {
                throw new EmulatorException(
                        String.format("Predicate request type was not supplied for response rule [%s]", responseRule));
            }
        }

        private void addAndPredicate(Predicate<HttpRequestContainer> andPredicate) {
            if (predicate == null) {
                predicate = andPredicate;
            } else {
                predicate = predicate.and(andPredicate);
            }
        }

        @Override
        public HttpResponseRule.LifeTimes getTimeToLive() {
            return this.timeToLive;
        }

        @Override
        public boolean test(HttpRequestContainer requestContainer) {
            return predicate.test(requestContainer);
        }

        @Override
        public List<String> getRules() {
            return predicateDefinition.getRules();
        }

        @Override
        public String getRequestType() {
            return predicateDefinition.getRequestType();
        }

        @Override
        public int getPriority() {
            return this.priority;
        }

        @Override
        public int hashCode() {
            return HashCodeBuilder.reflectionHashCode(this);
        }

        @Override
        public boolean equals(Object o) {
            return EqualsBuilder.reflectionEquals(this, o);
        }

        @Override
        public String toString() {
            return ToStringBuilder.reflectionToString(this, ToStringStyle.JSON_STYLE);
        }
    }
}
