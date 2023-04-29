package com.homer.exception;

public class HomerException extends RuntimeException {

    public HomerException(String message) {
        super(message);
    }

    public HomerException(String message, Throwable cause) {
        super(message, cause);
    }
}
