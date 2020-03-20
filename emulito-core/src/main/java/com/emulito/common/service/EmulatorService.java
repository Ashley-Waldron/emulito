package com.emulito.common.service;

import com.emulito.common.domain.predicates.RequestPredicate;
import com.emulito.common.exception.EmulatorException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.emulito.common.domain.http.HttpRequestContainer;
import com.emulito.common.domain.http.HttpResponseRule;
import com.emulito.common.domain.http.HttpResponseContainer;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.FileNotFoundException;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import static java.lang.String.format;

/**
 * Created by Ashley Waldron (e062130) on 12/14/2018.
 */
@Component
public class EmulatorService {
    private static final Logger LOG = LoggerFactory.getLogger(EmulatorService.class);
    private static final String EMULATOR_PRESET_RESPONSE_FILE_EXTENSION = ".json";
    private static final String DEFAULT_RESPONSES_DIRECTORY_PATTERN = "responses/defaults/**";

    @Autowired
    private RequestResponseHandler requestResponseHandler;

    @Autowired
    private PredicateBuilder predicateBuilder;

    private final Set<RequestPredicate> requestPredicates = new TreeSet<>(priorityComparator());

    private ObjectMapper objectMapper = new ObjectMapper();

    @PostConstruct
    public void init() {
        loadDefaultResponses();
    }

    private Comparator<RequestPredicate> priorityComparator() {
        return (o1, o2) -> {
            if(o1.equals(o2)) {
                return 0;
            } else {
                return (o2.getPriority() > o1.getPriority()) ? 1 : -1;
            }
        };
    }

    private void loadDefaultResponses() {
        LOG.info("Loading default emulator responses from directory [{}]", DEFAULT_RESPONSES_DIRECTORY_PATTERN);
        try {
            PathMatchingResourcePatternResolver scanner = new PathMatchingResourcePatternResolver();
            Resource[] resources = scanner.getResources(DEFAULT_RESPONSES_DIRECTORY_PATTERN);
            for (Resource resource : resources) {
                if (resource.getFilename() != null && resource.getFilename().endsWith(EMULATOR_PRESET_RESPONSE_FILE_EXTENSION)) {
                    LOG.debug("Loading default emulator response file [{}]", resource.getFilename());
                    HttpResponseRule defaultResponse = objectMapper.readValue(
                            IOUtils.toString(resource.getInputStream(), StandardCharsets.UTF_8),
                            HttpResponseRule.class);
                    addResponseForPredicate(defaultResponse);
                }
            }
        } catch (FileNotFoundException e) {
            throw new EmulatorException(String.format("Could not find the default responses folder path on " +
                            "the classpath. Please make sure that the folder path '%s' exists somewhere on the classpath " +
                            "and contains valid response files with %s extensions",
                    DEFAULT_RESPONSES_DIRECTORY_PATTERN, EMULATOR_PRESET_RESPONSE_FILE_EXTENSION), e);
        } catch (Exception e) {
            throw new EmulatorException("An error occurred trying to load the default responses", e);
        }
    }


    public void reset() {
        synchronized (requestPredicates) {
            Set<RequestPredicate> predicatesToRemove = requestPredicates.stream()
                    .filter(requestPredicate -> requestPredicate.getTimeToLive() != HttpResponseRule.LifeTimes.FOREVER)
                    .collect(Collectors.toSet());
            requestResponseHandler.reset(predicatesToRemove);
            requestPredicates.removeAll(predicatesToRemove);
        }
    }

    public HttpRequestContainer getLastApplicationRequest(String requestType) {
        return requestResponseHandler.getLastApplicationRequest(requestType);
    }

    public void addResponseRule(HttpResponseRule responseRule) {
        LOG.info("Adding response rule:\n[{}]", responseRule);
        synchronized (requestPredicates) {
            addResponseForPredicate(responseRule);
        }
    }

    public HttpResponseContainer getResponse(HttpRequestContainer requestContainer) {
        LOG.info("Application Request received was: [{}]", requestContainer);

        RequestPredicate requestPredicate;
        HttpResponseRule nextResponse;
        synchronized (requestPredicates) {
            requestPredicate = getPredicateForRequest(requestContainer);
            if (requestPredicate.getTimeToLive() == HttpResponseRule.LifeTimes.SINGLE_USE) {
                LOG.debug("Getting temporary response set up for predicate [{}]", requestPredicate);
                nextResponse = requestResponseHandler.removeNextResponse(requestPredicate);
                requestPredicates.remove(requestPredicate);
            } else {
                LOG.debug("Getting default/permanent response set up for predicate [{}]", requestPredicate);
                nextResponse = requestResponseHandler.getNextResponse(requestPredicate);
            }
        }
        requestResponseHandler.setLastApplicationRequest(requestPredicate.getRequestType(), requestContainer);

        if (nextResponse == null) {
            throw new EmulatorException(
                    String.format(
                            "Received an unknown application request. Please ensure that there " +
                                    "is a default response set up which will match the following request [%s]", requestContainer));
		}
        sleep(nextResponse);
        LOG.info("Replying with response [{}]", nextResponse);
        return nextResponse.getResponse();
    }

    private void sleep(HttpResponseRule nextResponse) {
        HttpResponseRule.Delay delay = nextResponse.getDelay();
        if (delay == null) {
            LOG.debug("No delay set for response");
            return;
        }
        LOG.debug("Waiting for preset [{}] milliseconds before returning.....", delay.getDelayTimeInMillis());
        if (delay.getDelayTimeInMillis() > 0) {
            try {
                Thread.sleep(delay.getDelayTimeInMillis());
            } catch (Exception e) {
                throw new EmulatorException("The thread was interrupted while implementing a response wait time", e);
            }
        }
    }

    private RequestPredicate getPredicateForRequest(HttpRequestContainer requestContainer) {
        return requestPredicates.stream()
                .filter(requestPredicate -> requestPredicate.test(requestContainer))
                .findFirst()
                .orElseThrow(()-> new EmulatorException(format("Unknown application request %s", requestContainer)));
    }

    private void addResponseForPredicate(HttpResponseRule responseRule) {
        LOG.debug("Adding new predicate/response to emulator for:\n[{}]", responseRule);
        RequestPredicate requestPredicate = predicateBuilder.buildPredicate(responseRule);
        requestPredicates.add(requestPredicate);
        LOG.debug("New predicate added to emulator:\n[{}]", requestPredicate);
        requestResponseHandler.setNextResponse(requestPredicate, responseRule);
        LOG.debug("New response added");
    }
}
