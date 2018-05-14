package com.belosh.portal.http;


// TODO: should be rewrited to use HttpServletResponse codes
public enum StatusCode {
    OK(200, "OK"),
    NOT_FOUND(404, "Not Found"),
    BAD_REQUEST(400, "Bad Request"),
    INTERNAL_SERVER_ERROR(500, "Internal Server Error"),
    FOUND(302, "Found");

    private final int statusCode;
    private final String statusMessage;

    StatusCode(int statusCode, String statusMessage) {
        this.statusCode = statusCode;
        this.statusMessage = statusMessage;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public static StatusCode getStatusCodeByCode(int code) {
        for (StatusCode statusCode : values()) {
            if (statusCode.statusCode == code) {
                return statusCode;
            }
        }
        throw new IllegalArgumentException("No HttpMethod with name " + code + " found");
    }
}
