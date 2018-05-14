package com.belosh.portal.parser;

import com.belosh.portal.exception.WebServerException;
import com.belosh.portal.http.HttpMethod;
import com.belosh.portal.http.PortalHttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;

import static com.belosh.portal.http.StatusCode.BAD_REQUEST;

public class RequestParser {
    private static final Logger logger = LoggerFactory.getLogger(RequestParser.class);

    public static PortalHttpServletRequest parseRequest(InputStream inputStream) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        PortalHttpServletRequest request = new PortalHttpServletRequest();

        parseGeneralHeader(request, reader);
        injectHeaders(request, reader);

        return request;
    }

    private static void parseGeneralHeader(PortalHttpServletRequest request, BufferedReader reader){
        String generalUrlAndHeader = null;
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
        request.setUrl(url);

        String[] splittedUrl = url.split("\\?");
        String resource = splittedUrl[0];
        String parameters = splittedUrl.length > 1 ? splittedUrl[1] : null;

        injectApplicationAndResource(request, resource);
        injectParams(request, parameters);
    }

    private static void injectParams(PortalHttpServletRequest request, String parameters) {
        if (parameters != null) {
            String[] paramPairs = parameters.split("[&=]");
            Map<String, String> paramsMap = request.getParameterMap();
            for (int i = 0; i < paramPairs.length - 1; i = i + 2) {
                paramsMap.put(paramPairs[i], paramPairs[i + 1]);
            }
        }
    }

    private static void injectHeaders(PortalHttpServletRequest request, BufferedReader reader) {
        String requestHeader;
        Map<String, String> map = request.getHeaderMap();
        try {
            while ((requestHeader = reader.readLine()) != null && !requestHeader.isEmpty()) {
                String[] splittedHeader = requestHeader.split(": ");
                map.put(splittedHeader[0], splittedHeader[1]);
            }
        } catch (IOException e) {
            throw new WebServerException("Unable to retrieve request headers", BAD_REQUEST);
        }
    }

    private static void injectApplicationAndResource(PortalHttpServletRequest request, String resourceUrl) {

        String[] splittedResource = resourceUrl.split("/", 3);
        int parametersCount = splittedResource.length;

        String application;
        String resource;

        if (parametersCount < 3) {
            if ("".equals(splittedResource[1])) {
                // Portal WebServer Start page case
                application = "ROOT";
                resource = "index.html";
            } else if (splittedResource[1].indexOf(".") > 0) {
                // Portal WebServer resource case
                application = "ROOT";
                resource = splittedResource[1];
            } else {
                // TODO: Application default page should be configured via app cfg
                String defaultResource = "index.html";
                application = splittedResource[1];
                resource = defaultResource;
                request.setRedirectPath(application + "/" + resource);
            }
        } else {
            application = splittedResource[1];
            resource = splittedResource[2];
        }

        request.setApplication(application);
        request.setResource("/" + resource);
    }
}
