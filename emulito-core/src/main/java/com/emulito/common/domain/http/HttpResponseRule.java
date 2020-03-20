package com.emulito.common.domain.http;

import com.emulito.common.web.controller.EmulatorController;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.ArrayList;
import java.util.List;

import static com.emulito.common.domain.http.HttpResponseRule.LifeTimes.FOREVER;
import static org.apache.commons.lang3.builder.ToStringStyle.JSON_STYLE;

/**
 * Created by Ashley Waldron (e062130) on 12/14/2018.
 * <br/>
 * <br/>
 * Object used for emulator representation of a HTTP Response along with the rules which should be followed in order
 * to decide to return that particular response.
 * <br/>
 * This is passed to the
 * {@value EmulatorController#RESPONSE_RULES_URL} call to
 * set all of the various properties of the next dynamically set specified application HTTP response.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class HttpResponseRule {

    public enum LifeTimes {
        SINGLE_USE,
        UNTIL_RESET,
        FOREVER
    }

    @JsonProperty("predicate")
    private CustomRequestPredicateDefinition predicate;

    @JsonProperty("priority")
    private int priority;

    @JsonProperty("timeToLive")
    private LifeTimes timeToLive = FOREVER;

    @JsonProperty("delay")
    private Delay delay;

    @JsonProperty("response")
    private HttpResponseContainer response;


    public CustomRequestPredicateDefinition getPredicate() {
        return predicate;
    }

    public void setPredicate(CustomRequestPredicateDefinition predicate) {
        this.predicate = predicate;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public LifeTimes getTimeToLive() {
        return timeToLive;
    }

    public void setTimeToLive(LifeTimes timeToLive) {
        this.timeToLive = timeToLive;
    }

    public Delay getDelay() {
        return delay;
    }

    public void setDelay(Delay delay) {
        this.delay = delay;
    }

    public HttpResponseContainer getResponse() {
        return response;
    }

    public void setResponse(HttpResponseContainer response) {
        this.response = response;
    }


    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, JSON_STYLE);
    }

    /**
     * Created by Ashley Waldron (e062130) on 12/14/2018.
     * <br/>
     * <br/>
     * Object used to represent the rules which should be followed in order to decide to return this particular response.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CustomRequestPredicateDefinition {

        @JsonProperty("requestType")
        private String requestType;

        @JsonProperty("rules")
        private List<String> rules = new ArrayList<>();

        public String getRequestType() {
            return requestType;
        }

        public void setRequestType(String requestType) {
            this.requestType = requestType;
        }

        public List<String> getRules() {
            return rules;
        }

        public void setRules(List<String> rules) {
            this.rules = rules;
        }

        @Override
        public String toString() {
            return ToStringBuilder.reflectionToString(this, JSON_STYLE);
        }
    }

    /**
     * Created by Ashley Waldron (e062130) on 13/3/2019.
     * <br/>
     * <br/>
     * Object used to represent any delay based functionality related to this particular response.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Delay {

        @JsonProperty("delayTimeInMillis")
        private long delayTimeInMillis = 0L;

        public long getDelayTimeInMillis() {
            return delayTimeInMillis;
        }

        public Delay setDelayTimeInMillis(long delayTimeInMillis) {
            this.delayTimeInMillis = delayTimeInMillis;
            return this;
        }

        @Override
        public String toString() {
            return ToStringBuilder.reflectionToString(this, JSON_STYLE);
        }
    }
}