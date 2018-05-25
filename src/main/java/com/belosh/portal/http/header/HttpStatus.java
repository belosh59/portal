package com.belosh.portal.http.header;


// TODO: should be rewrited to use HttpServletResponse codes
public enum HttpStatus {
    OK(200, "OK"),
    NOT_FOUND(404, "Not Found"),
    BAD_REQUEST(400, "Bad Request"),
    INTERNAL_SERVER_ERROR(500, "Internal Server Error"),
    FOUND(302, "Found"),
    UNAUTHORIZED(401, "Unauthorized");

    private final int statusCode;
    private final String statusMessage;

    HttpStatus(int statusCode, String statusMessage) {
        this.statusCode = statusCode;
        this.statusMessage = statusMessage;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public static HttpStatus getStatusByCode(int code) {
        for (HttpStatus httpStatus : values()) {
            if (httpStatus.statusCode == code) {
                return httpStatus;
            }
        }
        throw new IllegalArgumentException("No HttpMethod with name " + code + " found");
    }
}
