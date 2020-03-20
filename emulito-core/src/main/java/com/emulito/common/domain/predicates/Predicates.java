package com.emulito.common.domain.predicates;

import com.emulito.common.domain.http.HttpRequestContainer;
import org.apache.commons.lang3.StringUtils;

import java.util.function.Predicate;

/**
 * Created by Ashley Waldron (e062130) on 1/10/2019.
 */
final class Predicates {

    private Predicates() {
    }

    static Predicate<HttpRequestContainer> matchesPredicate(ValueExtractor<HttpRequestContainer> valueExtractor, String requiredValue) {
        return o -> (valueExtractor.get(o) == null && requiredValue == null) ||
                (valueExtractor.get(o) != null && valueExtractor.get(o).matches(requiredValue));
    }

    static Predicate<HttpRequestContainer> startsWithPredicate(ValueExtractor<HttpRequestContainer> valueExtractor, String requiredValue) {
        return o -> StringUtils.startsWith(valueExtractor.get(o), requiredValue);
    }

    static Predicate<HttpRequestContainer> endsWithPredicate(ValueExtractor<HttpRequestContainer> valueExtractor, String requiredValue) {
        return o -> StringUtils.endsWith(valueExtractor.get(o), requiredValue);
    }

    static Predicate<HttpRequestContainer> containsPredicate(ValueExtractor<HttpRequestContainer> valueExtractor, String requiredValue) {
        return o -> StringUtils.contains(valueExtractor.get(o), requiredValue);
    }

    static Predicate<HttpRequestContainer> equalsPredicate(ValueExtractor<HttpRequestContainer> valueExtractor, String requiredValue) {
        return o -> StringUtils.equals(valueExtractor.get(o), requiredValue);
    }
}