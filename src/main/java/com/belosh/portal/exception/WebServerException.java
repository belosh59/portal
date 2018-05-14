package com.belosh.portal.exception;

import com.belosh.portal.http.StatusCode;

public class WebServerException extends RuntimeException {
    private StatusCode errorStatus;
    private String message;

    public WebServerException(String message, Throwable cause, StatusCode errorStatus) {
        super(message, cause);
        this.errorStatus = errorStatus;
        this.message = message;
    }

    public WebServerException(String message, StatusCode errorStatus) {
        super(message);
        this.errorStatus = errorStatus;
        this.message = message;
    }

    public StatusCode getErrorStatus() {
        return errorStatus;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
