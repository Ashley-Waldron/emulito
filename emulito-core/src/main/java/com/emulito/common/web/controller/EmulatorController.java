package com.emulito.common.web.controller;

import com.emulito.common.exception.ApplicationRequestNotFoundException;
import com.emulito.common.exception.EmulatorException;
import com.emulito.common.service.EmulatorService;
import com.emulito.common.domain.http.HttpRequestContainer;
import com.emulito.common.domain.http.HttpResponseContainer;
import com.emulito.common.domain.http.HttpResponseRule;
import com.emulito.common.utils.RequestUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHeaders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * Global Controller.
 */
@RestController
@RequestMapping("/")
public class EmulatorController {

    private static final Logger LOG = LoggerFactory.getLogger(EmulatorController.class);
    private static final String APP_REQUESTS_URL = "/applicationRequest";
    private static final String PRESET_RESPONSE_URL = "/presetResponse";
    private static final String RESET_URL = "/reset";
    private static final String REQUEST_TYPE_URL_PARAM_KEY = "requestType";
    private static final String URI_OVERRIDE_HEADER = "X-Request-URI-Override";

    @Autowired
    private EmulatorService emulatorService;

    @Autowired
    private RequestUtils requestUtils;

    @RequestMapping(
            value = "/**",
            method = {RequestMethod.POST, RequestMethod.PUT,
                    RequestMethod.GET, RequestMethod.DELETE,
                    RequestMethod.OPTIONS, RequestMethod.PATCH,
                    RequestMethod.TRACE, RequestMethod.HEAD})
    public void handleRequest(HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        getResponse(httpRequest, httpResponse);
    }

    private void getResponse(HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        String uriOverride = httpRequest.getHeader(URI_OVERRIDE_HEADER);
        LOG.info("Application request received for URI [{}], {} [{}]", httpRequest.getRequestURI(), URI_OVERRIDE_HEADER, uriOverride);
        HttpRequestContainer requestContainer;
        if (!StringUtils.isEmpty(uriOverride)) {
            requestContainer = requestUtils.buildRequestContainer(httpRequest, uriOverride);
        } else {
            requestContainer = requestUtils.buildRequestContainer(httpRequest);
        }
        HttpResponseContainer response = emulatorService.getResponse(requestContainer);
        returnResponse(httpResponse, response);
    }

    private void returnResponse(HttpServletResponse httpResponse, HttpResponseContainer response) {
        httpResponse.setStatus(response.getStatusCode());
        for (Map.Entry<String, List<String>> headers : response.getHeaders().entrySet()) {
            for (String headerValue : headers.getValue()) {
                //don't add any content-length header as that will be populated automatically
                if (!HttpHeaders.CONTENT_LENGTH.equals(headerValue)) {
                    httpResponse.addHeader(headers.getKey(), headerValue);
                }
            }
        }
        try {
            httpResponse.getWriter().write(response.getBody());
            httpResponse.getWriter().flush();
            LOG.info("Response sent");
        } catch (IOException e) {
            throw new EmulatorException("There was an error writing the http response body", e);
        }
    }

    /**
     * API call to clear out the stored emulator state. Should be called at the start of every application test
     */
    @DeleteMapping(
            value = RESET_URL)
    public void resetEmulator() {
        LOG.info("Emulator Request received from test client to reset the emulator");
        emulatorService.reset();
        LOG.info("Emulator state reset");
    }

    /**
     * API call to obtain the last request received by this emulator for the specified request type.
     * Request type passed in by the {@value #REQUEST_TYPE_URL_PARAM_KEY} URL parameter.
     * <br/>
     * For a sample request file see resources/responses/sampleRequestTemplate.json
     */
    @GetMapping(
            value = APP_REQUESTS_URL,
            produces = APPLICATION_JSON_VALUE)
    @ResponseBody
    public HttpRequestContainer getLastApplicationRequest(@RequestParam(REQUEST_TYPE_URL_PARAM_KEY) String requestType) {
        LOG.info("Emulator Request received from test client for last application request [{}]", requestType);
        if(StringUtils.isEmpty(requestType)) {
            throw new HttpClientErrorException(BAD_REQUEST, format("'%s' URL param was not set", REQUEST_TYPE_URL_PARAM_KEY));
        }
        HttpRequestContainer lastRequest = emulatorService.getLastApplicationRequest(requestType);
        if(lastRequest == null) {
            throw new ApplicationRequestNotFoundException(format("[%s] request was never sent to the emulator", requestType));
        }
        LOG.info("Returning last application request:\n [{}]", lastRequest);
        return lastRequest;
    }

    /**
     * API call to add a response rule which defines what the emulator should return for requests that match the rule predicate.
     * For a sample response setup file see resources/responses/sampleResponseSetupTemplate.json
     */
    @PostMapping(
            value = PRESET_RESPONSE_URL)
    public void addResponseRule(@RequestBody HttpResponseRule responseRule) {
        LOG.info("Emulator Request received from test client to add a response rule");
        emulatorService.addResponseRule(responseRule);
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)  // 404
    @RequestMapping(
            value = "/cloudfoundryapplication/**",
            method = {RequestMethod.POST, RequestMethod.PUT,
                    RequestMethod.GET, RequestMethod.DELETE,
                    RequestMethod.OPTIONS, RequestMethod.PATCH,
                    RequestMethod.TRACE, RequestMethod.HEAD})
    public void handlePCFRequest() {
    }

}
