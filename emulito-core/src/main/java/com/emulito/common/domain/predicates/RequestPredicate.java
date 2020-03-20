package com.emulito.common.domain.predicates;

import com.emulito.common.domain.http.HttpResponseRule;
import com.emulito.common.domain.http.HttpRequestContainer;

import java.util.List;
import java.util.function.Predicate;

/**
 * Created by Ashley Waldron (e062130) on 12/14/2018.
 */
public interface RequestPredicate extends Predicate<HttpRequestContainer> {

    List<String> getRules();

    HttpResponseRule.LifeTimes getTimeToLive();

    String getRequestType();

    int getPriority();
}
