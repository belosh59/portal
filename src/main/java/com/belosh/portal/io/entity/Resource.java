package com.belosh.portal.io.entity;

import java.io.InputStream;
import java.nio.file.Path;

public class Resource {
    private Path resourcePath;
    private InputStream content;
    private String contentType;
    private long contentLength;

    public Path getResourcePath() {
        return resourcePath;
    }

    public void setResourcePath(Path resourcePath) {
        this.resourcePath = resourcePath;
    }

    public InputStream getContent() {
        return content;
    }

    public void setContent(InputStream content) {
        this.content = content;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public long getContentLength() {
        return contentLength;
    }

    public void setContentLength(long contentLength) {
        this.contentLength = contentLength;
    }

    @Override
    public String toString() {
        return "Resource{" +
                "resourcePath=" + resourcePath +
                ", content=" + content +
                ", contentType='" + contentType + '\'' +
                ", contentLength=" + contentLength +
                '}';
    }
}
