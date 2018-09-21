package com.belosh.portal.http.parser;

import com.belosh.portal.application.ApplicationManager;
import com.belosh.portal.application.entity.Application;
import com.belosh.portal.exception.WebServerException;
import com.belosh.portal.http.header.HttpMethod;
import com.belosh.portal.http.entity.PortalServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.Cookie;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;

import static com.belosh.portal.http.header.HttpStatus.BAD_REQUEST;
import static com.belosh.portal.http.header.HttpStatus.INTERNAL_SERVER_ERROR;

public class RequestParser {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private static final String SEPARATOR = "/";
    private static final String DELIMITER = ": ";
    private static final String DEFAULT_APPLICATION = "ROOT";
    private static final String DEFAULT_PAGE = "index.html";
    private final ApplicationManager applicationManager;

    public RequestParser(ApplicationManager applicationManager) {
        this.applicationManager = applicationManager;
    }

    public PortalServletRequest parseRequest(InputStream inputStream) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        PortalServletRequest request = new PortalServletRequest();

        parseGeneralHeader(request, reader);
        injectHeaders(request, reader);
        injectCookies(request);

        if (request.getMethod().matches("POST|PUT")) {
            injectBody(request, reader);
        }

        return request;
    }

    private void parseGeneralHeader(PortalServletRequest request, BufferedReader reader){
        String generalUrlAndHeader;
        try {
            generalUrlAndHeader = reader.readLine();
            logger.info("Request header: " + generalUrlAndHeader);
            if (generalUrlAndHeader == null) {
                throw new WebServerException("Invalid request header", BAD_REQUEST);
            }
        } catch (IOException e) {
            throw new WebServerException("Unable to read request header general URL and Method", BAD_REQUEST);
        }

        String[] splittedMethodAndUrl = generalUrlAndHeader.split("[ ?]");
        String method = splittedMethodAndUrl[0];
        String url = splittedMethodAndUrl[1];

        request.setMethod(HttpMethod.getHttpMethodByName(method));
        request.setRequestURI(url);

        String[] splittedUrl = url.split("\\?");
        String requestURI = splittedUrl[0];
        String parameters = splittedUrl.length > 1 ? splittedUrl[1] : null;

        injectApplicationAndResource(request, requestURI);
        injectParams(request, parameters);
    }

    private void injectParams(PortalServletRequest request, String parameters) {
        if (parameters != null) {
            String[] paramPairs = parameters.split("[&=]");
            Map<String, String> paramsMap = request.getParameters();
            for (int i = 0; i < paramPairs.length - 1; i = i + 2) {
                paramsMap.put(paramPairs[i], paramPairs[i + 1]);
            }
        }
    }

    private void injectHeaders(PortalServletRequest request, BufferedReader reader) {
        String requestHeader;
        Map<String, String> headers = request.getHeaderMap();
        try {
            while ((requestHeader = reader.readLine()) != null && !requestHeader.isEmpty()) {
                String[] splittedHeader = requestHeader.split(DELIMITER);
                logger.debug("Request headers: {} - - {} ", splittedHeader[0], splittedHeader[1]);
                headers.put(splittedHeader[0], splittedHeader[1]);
            }
        } catch (IOException e) {
            throw new WebServerException("Unable to retrieve request headers", BAD_REQUEST);
        }
    }

    private void injectApplicationAndResource(PortalServletRequest request, String requestURI) {
        String[] splittedResourceUrl = requestURI.split(SEPARATOR, 3);

        String applicationName = splittedResourceUrl[1];
        String resourceURI = splittedResourceUrl.length > 2 ? splittedResourceUrl[2] : SEPARATOR;

        Application application = applicationManager.getApplication(applicationName);
        if (application == null) {
            applicationName = DEFAULT_APPLICATION;

            if (SEPARATOR.equals(requestURI)) {
                resourceURI = DEFAULT_PAGE; // Portal WebServer welcome page case
            } else {
                resourceURI = requestURI;
            }
            application = applicationManager.getApplication(applicationName);
        }

        request.setServletPath(SEPARATOR + resourceURI);
        request.setApplication(application);
        request.setContextPath(SEPARATOR + applicationName);
    }


    private void injectBody(PortalServletRequest request, BufferedReader reader) {
        try {
            StringBuilder payload = new StringBuilder();
            while (reader.ready()) {
                payload.append((char) reader.read());
            }
            injectParams(request, payload.toString());
        } catch (IOException e) {
            logger.error("Unable to parse request body");
            throw new WebServerException("Unable to parse request body", INTERNAL_SERVER_ERROR);
        }
    }

    private void injectCookies(PortalServletRequest request) {
        String cookiesHeader = request.getHeader("Cookie");

        if (cookiesHeader != null) {
            String[] splittedCoockies = cookiesHeader.split("=|; ");


            Cookie[] cookies = new Cookie[splittedCoockies.length / 2];
            int cookieCounter = 0;
            for (int i = 0; i < splittedCoockies.length - 1; i = i + 2) {
                cookies[cookieCounter] = new Cookie(splittedCoockies[i], splittedCoockies[i + 1]);
                cookieCounter++;
            }

            request.addCookies(cookies);
        }
    }
}
