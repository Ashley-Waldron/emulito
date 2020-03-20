package com.emulito.common.utils;

import com.emulito.common.exception.EmulatorException;
import com.emulito.common.domain.http.HttpRequestContainer;
import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Ashley Waldron (e062130) on 12/12/2018.
 */
@Component
public class RequestUtils {

    public HttpRequestContainer buildRequestContainer(HttpServletRequest request) {
        HttpRequestContainer httpRequestContainer = new HttpRequestContainer();

        httpRequestContainer.setHttpMethod(request.getMethod());
        httpRequestContainer.setUrl(request.getRequestURI());

        setHeaders(request, httpRequestContainer);
        setBody(request, httpRequestContainer);

        // getParameterMap() must be called after "setBody", because otherwise, for content type "application/x-www-form-urlencoded", it will consume the body
        Map<String, List<String>> parameterMap = new HashMap<>();
        request.getParameterMap().forEach((key, values) -> parameterMap.put(key, Arrays.asList(values)));
        httpRequestContainer.setParams(parameterMap);

        return httpRequestContainer;
    }

    public HttpRequestContainer buildRequestContainer(HttpServletRequest request, String uriOverride) {
        HttpRequestContainer httpRequestContainer = new HttpRequestContainer();

        httpRequestContainer.setHttpMethod(request.getMethod());
        UriComponents uriComponents = UriComponentsBuilder.fromUriString(uriOverride).build();
        httpRequestContainer.setUrl(uriComponents.getPath());

        httpRequestContainer.setParams(uriComponents.getQueryParams());

        setHeaders(request, httpRequestContainer);
        setBody(request, httpRequestContainer);

        return httpRequestContainer;
    }


    private static void setHeaders(HttpServletRequest request, HttpRequestContainer httpRequestContainer) {
        Map<String, List<String>> headers = new HashMap<>();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            headers.put(headerName, Collections.list(request.getHeaders(headerName)));
        }
        httpRequestContainer.setHeaders(headers);
    }

    private static void setBody(HttpServletRequest request, HttpRequestContainer httpRequestContainer) {
        try {
            httpRequestContainer.setBody(IOUtils.toString(request.getReader()));
        } catch (IOException e) {
            throw new EmulatorException("There was an error parsing the body of the application request", e);
        }
    }
}
