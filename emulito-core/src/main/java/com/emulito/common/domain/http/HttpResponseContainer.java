package com.emulito.common.domain.http;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.apache.commons.lang3.builder.ToStringStyle.JSON_STYLE;

/**
 * Created by Ashley Waldron (e062130) on 12/14/2018.
 * <br/>
 * <br/>
 * Object used to represent the actual values to return in the HTTP Response.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class HttpResponseContainer {

    @JsonProperty("statusCode")
    private int statusCode;

    @JsonProperty("headers")
    private Map<String, List<String>> headers = new HashMap<>();

    @JsonProperty("body")
    private String body;

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setHeaders(Map<String, List<String>> headers) {
        this.headers = new HashMap<>(headers);
    }

    public Map<String, List<String>> getHeaders() {
        return new HashMap<>(headers);
    }

    public void addHeader(String name, String value) {
        if(!headers.containsKey(name)) {
            headers.put(name, new ArrayList<>());
        }
        headers.get(name).add(value);
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getBody() {
        return body;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, JSON_STYLE);
    }
}