package com.belosh.portal;

import com.belosh.portal.entity.Application;
import com.belosh.portal.exception.WebServerException;
import com.belosh.portal.http.PortalHttpServletRequest;
import com.belosh.portal.http.PortalHttpServletResponse;
import com.belosh.portal.http.StatusCode;
import com.belosh.portal.parser.RequestParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import java.io.*;
import java.net.Socket;

public class RequestHandler implements Runnable {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private Socket socket;
    private ApplicationManager applicationManager;

    public void handle() {
        try (InputStream inputStream = socket.getInputStream();
             OutputStream outputStream = socket.getOutputStream()) {
            try {
                PortalHttpServletRequest request = RequestParser.parseRequest(inputStream);
                PortalHttpServletResponse response = new PortalHttpServletResponse(outputStream);

                process(request, response);
            } catch (WebServerException e) {
                processErrorResponse(outputStream, e);
            } catch (Exception exception) {
                processErrorResponse(outputStream, StatusCode.INTERNAL_SERVER_ERROR);
                exception.printStackTrace();

            }
        } catch (IOException e) {
            logger.error("Unable to build socket streams");
            e.printStackTrace();
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                logger.error("Unable to close the socket");
                e.printStackTrace();
            }
        }
    }

    public void process(PortalHttpServletRequest request, PortalHttpServletResponse response) {
        String applicationName = request.getApplication();
        String resource = request.getResource();
        String redirectPath = request.getRedirectPath();

        Application application = applicationManager.getApplication(applicationName);
        if (application == null) {
            application = applicationManager.getApplication("ROOT");
            resource = "/" + applicationName + resource; // WebServer resource path. No application specified
        } else if (redirectPath != null) {
            try {
                response.sendRedirect(redirectPath);
                return; // Do not need to process current request resource if redirect was requested
            } catch (IOException e) {
                throw new WebServerException("Unable to send redirect", StatusCode.INTERNAL_SERVER_ERROR);
            }
        }

        HttpServlet servlet = application.getServletByResource(resource);
        if (servlet != null) {
            try {
                servlet.service(request, response);
                response.getWriter().flush();
            } catch (ServletException | IOException e) {
                logger.error("Unable to process servlet: " + servlet.getServletName());
                throw new WebServerException("Unable to process servlet", StatusCode.INTERNAL_SERVER_ERROR);
            }
        } else {
            String resourcePath = application.getApplicationFile().getAbsolutePath() + resource;
            processStaticResource(response, resourcePath);
        }
    }

    private void processStaticResource(PortalHttpServletResponse response, String resource) {
        logger.info("Processing static resource: {}", resource);
        byte[] buffer = new byte[1024];
        int bytesRead;

        try (BufferedInputStream content = new BufferedInputStream(new FileInputStream(resource))) {
            response.setStatus(200);
            while ((bytesRead = content.read(buffer)) > 0) {
                response.getOutputStream().write(buffer, 0, bytesRead);
            }
            response.getOutputStream().flush();
        } catch (IOException e) {
            logger.error("Resource not found: {}", resource);
            throw new WebServerException("Resource not found: " + resource, StatusCode.NOT_FOUND);
        }
    }

    private void processErrorResponse(OutputStream socketOutputStream, int statusCode, String message) throws IOException {
        String response = "HTTP/1.1 " + statusCode + " " + message + "\n\n";
        socketOutputStream.write(response.getBytes());
        logger.error("Issue processing request: {}, error status: {}", message, statusCode);
    }

    private void processErrorResponse(OutputStream socketOutputStream, StatusCode statusCode) throws IOException {
        processErrorResponse(socketOutputStream, statusCode.getStatusCode(), statusCode.getStatusMessage());
    }

    private void processErrorResponse(OutputStream socketOutputStream, WebServerException exception) throws IOException {
        int statusCode = exception.getErrorStatus().getStatusCode();
        processErrorResponse(socketOutputStream, statusCode, exception.getMessage());
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    public void setApplicationManager(ApplicationManager applicationManager) {
        this.applicationManager = applicationManager;
    }

    @Override
    public void run() {
        handle();
    }
}