package com.emulito.common.service;

import com.emulito.common.domain.predicates.RequestPredicate;
import com.google.common.collect.Queues;
import com.emulito.common.domain.http.HttpRequestContainer;
import com.emulito.common.domain.http.HttpResponseRule;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Ashley Waldron (e062130) on 12/14/2018.
 * <br/>
 * <br/>
 * Class that provides access to the requests that came into the emulator from the application
 * and also allows test clients to setup specific responses for particular request types
 */
@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
class RequestResponseHandler {

    private static final int MAX_STORED_APPLICATION_REQUESTS = 100;

    private Map<String, Deque<HttpRequestContainer>> applicationRequests = new ConcurrentHashMap<>();
    private Map<RequestPredicate, HttpResponseRule> nextResponses = new HashMap<>();

    /**
     * Returns the last application request that came in to the emulator for the specified request type.
     * @param requestType The type of request to retrieve
     * @return A {@link HttpRequestContainer} object containing the request data
     */
    HttpRequestContainer getLastApplicationRequest(String requestType) {
        return getApplicationRequests(requestType).pollLast();
    }

    void setLastApplicationRequest(String requestType, HttpRequestContainer request) {
        getApplicationRequests(requestType).addLast(request);
    }

    void setNextResponse(RequestPredicate requestPredicate, HttpResponseRule response) {
        nextResponses.put(requestPredicate, response);
    }

    HttpResponseRule getNextResponse(RequestPredicate requestPredicate) {
        return nextResponses.get(requestPredicate);
    }

    HttpResponseRule removeNextResponse(RequestPredicate requestPredicate) {
        return nextResponses.remove(requestPredicate);
    }

    private Deque<HttpRequestContainer> getApplicationRequests(String requestType) {
        return applicationRequests.computeIfAbsent(
                requestType,
                k -> Queues.synchronizedDeque(new EvictingDequeue(MAX_STORED_APPLICATION_REQUESTS)));
    }

    void reset(Set<RequestPredicate> tempRequestPredicates) {
        applicationRequests.clear();
        nextResponses.keySet().removeIf(tempRequestPredicates::contains);
    }

    private class EvictingDequeue extends ArrayDeque<HttpRequestContainer> {
        private final int maxSize;

        EvictingDequeue(int maxSize) {
            this.maxSize = maxSize;
        }

        @Override
        public void addLast(HttpRequestContainer httpRequestContainer) {
            if (size() >= maxSize) {
               removeFirst();
            }
            super.addLast(httpRequestContainer);
        }
    }
}