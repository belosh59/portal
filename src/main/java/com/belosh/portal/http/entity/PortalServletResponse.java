package com.belosh.portal.http.entity;

import com.belosh.portal.http.adapter.HttpServletResponseAdapter;
import com.belosh.portal.http.header.HttpStatus;
import com.belosh.portal.http.util.HeaderGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.Cookie;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class PortalServletResponse extends HttpServletResponseAdapter implements Closeable {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private PortalServletOutputStream servletOutputStream;
    private PrintWriter servletPrintWriter;
    private HttpStatus status;
    private Map<String, String> responseHeaders;
    private int bufferContentLength;
    private Cookie cookie;

    public PortalServletResponse(OutputStream socketOutputStream) {
        servletOutputStream = new PortalServletOutputStream(socketOutputStream);
        servletPrintWriter = new PrintWriter(new BufferedWriter(new OutputStreamWriter(servletOutputStream)));
        status = HttpStatus.OK;
        responseHeaders = new HashMap<>();
    }

    @Override
    public ServletOutputStream getOutputStream() {
        return servletOutputStream;
    }

    @Override
    public PrintWriter getWriter() {
        return servletPrintWriter;
    }

    @Override
    public void setStatus(int code) {
        try {
            status = HttpStatus.getStatusByCode(code);
        } catch (IllegalStateException e) {
            logger.error("Invalid status code specified: " + code);
        }
    }

    @Override
    public void setContentType(String contentType) {
        responseHeaders.put("Content-Type", contentType);
    }

    @Override
    public void setContentLengthLong(long length) {
        responseHeaders.put("Content-Length", Long.toString(length));
    }

    @Override
    public void sendRedirect(String redirectUrl)  {
        status = HttpStatus.FOUND;
        responseHeaders.put("Location", redirectUrl);
    }

    @Override
    public void setHeader(String key, String value) {
        responseHeaders.put(key, value);
    }

    @Override
    public String getHeader(String key) {
        return responseHeaders.get(key);
    }

    @Override
    public boolean containsHeader(String key) {
        return responseHeaders.containsKey(key);
    }

    public void setChunkedTransferEncoding() {
        responseHeaders.put("Transfer-Encoding", "chunked");
    }

    public Map<String, String> getResponseHeaders() {
        return responseHeaders;
    }

    public HttpStatus getStatusCode() {
        return status;
    }

    public int getBufferContentLength() {
        return bufferContentLength;
    }

    @Override
    public void addCookie(Cookie cookie) {
        this.cookie = cookie;
    }

    public Cookie getCookie() {
        return cookie;
    }

    @Override
    public void close() throws IOException {
        servletPrintWriter.flush();
        servletOutputStream.flush();
    }

    class PortalServletOutputStream extends ServletOutputStream {
        private static final int DEFAULT_BUFFER_SIZE = 1024 * 16;
        private byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
        private int index;
        private OutputStream socketOutputStream;
        boolean headersSent = false;
//        boolean chunkEncoding = false;

        public PortalServletOutputStream(OutputStream socketOutputStream) {
            this.socketOutputStream = socketOutputStream;
        }

        @Override
        public void write(int b) throws IOException {
            if (index >= buffer.length) {
//              chunkEncoding = true;
                flush();
            }
            buffer[index++] = (byte) b;
        }

        @Override
        public void flush() throws IOException {
            sendHeaders();

            if (index > 0) {
//                if (chunkEncoding) {
//                    String chunkHexLength = Integer.toHexString(index) + "\r\n";
//                    socketOutputStream.write(chunkHexLength.getBytes());
//                }

                socketOutputStream.write(buffer, 0, index);

//                if (chunkEncoding) {
//                    String chunkTerminator = "\r\n";
//                    socketOutputStream.write(chunkTerminator.getBytes());
//
//                    if (index < buffer.length) {
//                        String lastChunk = "0\r\n\r\n";
//                        socketOutputStream.write(lastChunk.getBytes());
//                    }
//                }

                index = 0;
            }
        }

        @Override
        public boolean isReady() {
            return true;
        }

        @Override
        public void setWriteListener(WriteListener writeListener) {

        }

        @Override
        public void close() throws IOException {
            flush();
        }

        private void sendHeaders() throws IOException {
            if (!headersSent) {
                PortalServletResponse response = PortalServletResponse.this;
                response.bufferContentLength = index;
//                if (chunkEncoding) {
//                    response.setChunkedTransferEncoding();
//                } else {
//                    response.bufferContentLength = index;
//                }

                byte[] headers = HeaderGenerator.getResponseHeaderBytes(response);
                socketOutputStream.write(headers, 0, headers.length);
                headersSent = true;
            }
        }
    }
}
