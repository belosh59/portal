package com.belosh.portal.http.util;

import com.belosh.portal.http.entity.PortalServletResponse;
import com.belosh.portal.http.header.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.Cookie;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Map;

public class HeaderGenerator {
    private static final Logger logger = LoggerFactory.getLogger(HeaderGenerator.class);
    private static final String PROTOCOL = "HTTP/1.1";
    private static final String DELIMITER = ": ";
    private static final String SPACE = " ";
    private static final String NEWLINE = "\n";

    // Default parameter keys
    private static final String SERVER_KEY = "Server";
    private static final String DATE_KEY = "Date";
    private static final String CONTENT_LENGTH_KEY = "Content-Length";
    private static final String TRANSFER_ENCODING = "Transfer-Encoding";
    private static final String SERVER_SIGNATURE = "Portal(0.0.1)";
    private static final String SET_COOKIES = "Set-Cookie ";


    public static byte[] getResponseHeaderBytes(PortalServletResponse response) {
        injectDefaultHeaders(response);

        HttpStatus responseStatus = response.getStatusCode();
        StringBuilder builder = new StringBuilder()
                .append(PROTOCOL + SPACE)
                .append(responseStatus.getStatusCode())
                .append(SPACE)
                .append(responseStatus.getStatusMessage())
                .append(NEWLINE);

        Map<String, String> responseHeaders = response.getResponseHeaders();
        for (Map.Entry entry : responseHeaders.entrySet()) {
            builder.append(entry.getKey());
            builder.append(DELIMITER);
            builder.append(entry.getValue());
            builder.append(NEWLINE);
        }

        builder.append(NEWLINE);

        logger.debug("Response headers:");
        logger.debug(builder.toString());

        return builder.toString().getBytes();
    }

//    public static byte[] getGeneralHeader(HttpStatus statusCode) {
//        return (PROTOCOL + SPACE + statusCode.getStatusCode() + SPACE + statusCode.getStatusMessage() + NEWLINE + NEWLINE).getBytes();
//    }

    private static void injectDefaultHeaders(PortalServletResponse response) {
        response.setHeader(SERVER_KEY, SERVER_SIGNATURE);
        
        if (!response.containsHeader(DATE_KEY)) {
            response.setHeader(DATE_KEY, java.time.format.DateTimeFormatter.RFC_1123_DATE_TIME.format(ZonedDateTime.now(ZoneId.of("GMT"))));
        }

        boolean isTransferEncoding = response.containsHeader(TRANSFER_ENCODING);
        boolean isContentLength = response.containsHeader(CONTENT_LENGTH_KEY);

        // TODO: Content Length should be omitted when transfer encoding specified
        if (!isTransferEncoding && !isContentLength ) {
            response.setHeader(CONTENT_LENGTH_KEY, Integer.toString(response.getBufferContentLength()));
        }

        Cookie cookie = response.getCookie();
        if (cookie != null) {
            response.setHeader(SET_COOKIES, cookie.getName() + "=" + cookie.getValue());
        }
    }


}
