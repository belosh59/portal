package com.belosh.portal.server.handler;

import com.belosh.portal.application.entity.Application;
import com.belosh.portal.chain.PortalFilterChain;
import com.belosh.portal.exception.WebServerException;
import com.belosh.portal.http.header.HttpStatus;
import com.belosh.portal.http.parser.RequestParser;
import com.belosh.portal.http.entity.PortalServletRequest;
import com.belosh.portal.http.entity.PortalServletResponse;
import com.belosh.portal.io.entity.Resource;
import com.google.common.io.ByteStreams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import java.io.*;
import java.net.Socket;

import static com.belosh.portal.http.header.HttpStatus.*;

public class RequestHandler implements Runnable {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final Socket socket;
    private final RequestParser requestParser;

    public RequestHandler(Socket socket, RequestParser requestParser) {
        this.socket = socket;
        this.requestParser = requestParser;
    }

    private void handle() {
        try (Socket s = socket) {
            InputStream inputStream = socket.getInputStream();
            OutputStream outputStream = socket.getOutputStream();

            //TODO: Required checking inputStream in terms of HTTP Timeout
            while (inputStream.available() > 0) {
                try (PortalServletResponse response = new PortalServletResponse(outputStream)) {
                    try {
                        PortalServletRequest request = requestParser.parseRequest(inputStream);

                        process(request, response);
                        logger.info("Request processing finished for: {}", request.getRequestURI());
                    } catch (WebServerException e) {
                        response.setStatus(e.getErrorStatus().getStatusCode());
                        logger.error("Error response sent. Status code {}; Reason {}", e.getErrorStatus().getStatusCode(), e.getMessage());
                    } catch (Throwable e) {
                        response.setStatus(INTERNAL_SERVER_ERROR.getStatusCode());
                        logger.error("Unhandled throwable exception. Request processing failed {}:", e);
                    }
                }
            }
        } catch (IOException e) {
            logger.error("Unable to build socket streams. Closing socket", e);
        }
    }

    private void process(PortalServletRequest request, PortalServletResponse response) {
        String redirectPath = request.getRedirectPath();
        if (redirectPath != null) {
            response.sendRedirect(redirectPath);
            return;
        }

        Application application = (Application) request.getServletContext();

        if (application.availableFilters() > 0) {
            processApplicationFilters(request, response, application);
            if (response.getStatusCode().equals(HttpStatus.FOUND)) {
                logger.info("Redirect requested in terms of HttpRequest filtering");
                return; // Interrupt processing if redirect requested after filtering
            }
        }

        processApplicationServlets(request, response, application);

    }

    private void processApplicationServlets(PortalServletRequest request,
                                            PortalServletResponse response,
                                            Application application) {
        String resourceURI = request.getServletPath();
        Servlet servlet = application.getServlet(resourceURI);
        if (servlet != null) {
            logger.info("Processing servlet. URL: {}", resourceURI);

            try {
                servlet.service(request, response);
            } catch (ServletException | IOException e) {
                logger.error("Unable to service servlet: {}", servlet.getServletInfo(), e);
                throw new WebServerException("Unable to service servlet", INTERNAL_SERVER_ERROR);
            }

        } else {
            String contextResourceURI = application.getContextPath() + resourceURI;
            logger.info("Processing static resource. URL: {}", contextResourceURI);

            Resource resource = application.getResourceByUrl(contextResourceURI);
            response.setContentType(resource.getContentType());
            response.setContentLengthLong(resource.getContentLength());

            try {
                InputStream inputStream = resource.getContent();
                OutputStream outputStream = response.getOutputStream();

                ByteStreams.copy(inputStream, outputStream);
            } catch (IOException e) {
                logger.error("Unable to read static resource: {}", resourceURI, e);
                throw new WebServerException("Unable to read static resource:", NOT_FOUND);
            }
        }
    }

    private void processApplicationFilters(PortalServletRequest request, PortalServletResponse response, Application application) {
        try {
            PortalFilterChain filterChain = new PortalFilterChain();
            filterChain.setApplicationFilters(application.getApplicationFilters());

            filterChain.doFilter(request, response);
        } catch (IOException | ServletException e) {
            logger.error("Unable to process filter chain", e);
            throw new WebServerException("Unable to process filter chain", INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public void run() {
        handle();
    }
}