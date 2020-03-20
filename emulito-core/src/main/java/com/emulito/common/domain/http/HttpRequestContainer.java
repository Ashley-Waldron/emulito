package com.emulito.common.domain.http;

import com.emulito.common.web.controller.EmulatorController;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.apache.commons.lang3.builder.ToStringStyle.JSON_STYLE;

/**
 * Created by Ashley Waldron (e062130) on 12/14/2018.
 * <br/>
 * <br/>
 * Object used for emulator representation of a HTTP Requests
 * <br/>
 * This is returned from the
 * {@value EmulatorController#APP_REQUESTS_URL} call to
 * represent all of the various properties of the last specified application HTTP request.
 * <br/>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class HttpRequestContainer {

    public static final String HTTP_METHOD_NODE_KEY = "httpMethod";
    public static final String URL_NODE_KEY = "url";
    public static final String URL_PARAMS_NODE_KEY = "urlParameters";
    public static final String HEADERS_NODE_KEY = "headers";
    public static final String BODY_NODE_KEY = "body";

    @JsonProperty(HTTP_METHOD_NODE_KEY)
    private String httpMethod;

    @JsonProperty(URL_NODE_KEY)
    private String url;

    @JsonProperty(URL_PARAMS_NODE_KEY)
    private Map<String, List<String>> params = new HashMap<>();

    @JsonProperty(HEADERS_NODE_KEY)
    private Map<String, List<String>> headers = new HashMap<>();

    @JsonProperty(BODY_NODE_KEY)
    private String body;

    public Map<String, List<String>> getHeaders() {
        return new HashMap<>(headers);
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getBody() {
        return body;
    }

    public void setHeaders(Map<String, List<String>> headers) {
        this.headers = headers;
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    public void setHttpMethod(String httpMethod) {
        this.httpMethod = httpMethod;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    public void setParams(Map<String, List<String>> params) {
        this.params = params;
    }

    public Map<String, List<String>> getParams() {
        return params;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, JSON_STYLE);
    }
}