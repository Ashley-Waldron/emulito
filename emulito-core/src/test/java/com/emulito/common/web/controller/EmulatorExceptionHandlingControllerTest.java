package com.emulito.common.web.controller;

import com.emulito.common.exception.ApplicationRequestNotFoundException;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import static org.hamcrest.CoreMatchers.is;

public class EmulatorExceptionHandlingControllerTest {

    private static final String TEST_MESSAGE = "TestMessage";

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @InjectMocks
    private EmulatorExceptionHandlingController emulatorExceptionHandlingController;

    @Test
    public void handleNotFoundRequest() {
        ApplicationRequestNotFoundException applicationRequestNotFoundException = new ApplicationRequestNotFoundException(TEST_MESSAGE);
        String response = emulatorExceptionHandlingController.handleNotFoundRequest(applicationRequestNotFoundException);
        Assert.assertThat(response, is("{\"message\":\"" + TEST_MESSAGE + "\"}"));
    }
}