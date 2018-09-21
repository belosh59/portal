package com.belosh.portal.exception;

import com.belosh.portal.http.header.HttpStatus;

public class WebServerException extends RuntimeException {
    private final HttpStatus errorStatus;
    private final String message;

    public WebServerException(String message, Throwable cause, HttpStatus errorStatus) {
        super(message, cause);
        this.errorStatus = errorStatus;
        this.message = message;
    }

    public WebServerException(String message, HttpStatus errorStatus) {
        super(message);
        this.errorStatus = errorStatus;
        this.message = message;
    }

    public HttpStatus getErrorStatus() {
        return errorStatus;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
