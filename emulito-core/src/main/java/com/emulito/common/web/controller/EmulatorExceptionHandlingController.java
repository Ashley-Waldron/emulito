package com.emulito.common.web.controller;

import com.emulito.common.exception.ApplicationRequestNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class EmulatorExceptionHandlingController {
	private static final Logger LOGGER = LoggerFactory.getLogger(EmulatorExceptionHandlingController.class);

	@ResponseStatus(HttpStatus.NOT_FOUND)  // 404
	@ResponseBody
	@ExceptionHandler(ApplicationRequestNotFoundException.class)
	public String handleNotFoundRequest(ApplicationRequestNotFoundException e) {
		LOGGER.debug("Handling application request not found exception");
		return String.format("{\"message\":\"%s\"}", e.getMessage());
	}
}