package com.belosh.portal.http;

import com.belosh.portal.http.adapter.PortalHttpServletResponseAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletOutputStream;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class PortalHttpServletResponse extends PortalHttpServletResponseAdapter {
    private  final Logger logger = LoggerFactory.getLogger(this.getClass());

    private StatusCode responseStatus = StatusCode.OK;
    private Map<String, String> responseHeaders = new HashMap<>();

    private OutputStream socketOutputStream;
    private PortalServletOutputStream portalServletOutputStream = new PortalServletOutputStream();
    private PortalServletPrintWriter printWriter = new PortalServletPrintWriter(portalServletOutputStream);
    private boolean headersSent = false;

    public PortalHttpServletResponse(OutputStream socketOutputStream) {
        this.socketOutputStream = socketOutputStream;
        // Set default response values
        responseHeaders.put("Server", "Portal(0.0.1)");
        // TODO: fix date preparation. LocalDateTime mask represented below
        responseHeaders.put("Date", "13 May 2018 18:59:15 GMT");
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        return portalServletOutputStream;
    }

    @Override
    public PrintWriter getWriter() {
        return printWriter;
    }

    @Override
    public void setStatus(int code) {
        try {
            responseStatus = StatusCode.getStatusCodeByCode(code);
        } catch (IllegalStateException e) {
            logger.error("Invalid status code specified: " + code + " . Default status code will be used.");
        }
    }

    @Override
    public void setContentType(String contentType) {
        responseHeaders.put("Content-Type", contentType);
    }

    @Override
    public void sendRedirect(String redirectPath) throws IOException {
        responseStatus = StatusCode.FOUND;
        responseHeaders.put("Location",redirectPath);
        portalServletOutputStream.flush();
    }

    private byte[] prepareHeaders() {
        StringBuilder builder = new StringBuilder();
        builder.append("HTTP/1.1 ");
        builder.append(responseStatus.getStatusCode());
        builder.append(" ");
        builder.append(responseStatus.getStatusMessage());
        builder.append("\n");

        for (Map.Entry entry : responseHeaders.entrySet()) {
            builder.append(entry.getKey());
            builder.append(": ");
            builder.append(entry.getValue());
            builder.append("\n");
        }

        builder.append("\n");

        return builder.toString().getBytes();
    }

    class PortalServletOutputStream extends ServletOutputStream {
        private static final int DEFAULT_BUFFER_SIZE = 8192;
        private byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
        private int index;

        @Override
        public void write(int b) throws IOException {
            if (index >= buffer.length) {
                flush();
            }
            buffer[index++] = (byte)b;
        }

        @Override
        public void flush() throws IOException {
            if (!headersSent) {
                byte[] headers = prepareHeaders();
                socketOutputStream.write(headers, 0, headers.length);
                headersSent = true;
            }
            if (index > 0) {
                socketOutputStream.write(buffer, 0, index);
                index = 0;
            }
        }
    }

    class PortalServletPrintWriter extends PrintWriter {
        public PortalServletPrintWriter(PortalServletOutputStream out) {
            super(new BufferedWriter(new OutputStreamWriter(out)));
        }
    }
}
