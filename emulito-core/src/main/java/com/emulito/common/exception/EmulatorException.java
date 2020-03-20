package com.emulito.common.exception;

/**
 * Created by Ashley Waldron (e062130) on 12/11/2018.
 */
public class EmulatorException extends RuntimeException {

    public EmulatorException(String message, Exception e) {
        super(message, e);
    }

    public EmulatorException(String message) {
        super(message);
    }
}
