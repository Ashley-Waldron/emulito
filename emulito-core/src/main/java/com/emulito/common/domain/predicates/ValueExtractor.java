package com.emulito.common.domain.predicates;

/**
 * Created by Ashley Waldron (e062130) on 1/10/2019.
 */
@FunctionalInterface
public interface ValueExtractor<T> {
    String get(T source);
}
