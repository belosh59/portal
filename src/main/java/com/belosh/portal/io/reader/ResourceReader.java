package com.belosh.portal.io.reader;

import com.belosh.portal.exception.WebServerException;
import com.belosh.portal.http.header.HttpStatus;
import com.belosh.portal.io.entity.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ResourceReader {

    private final Logger LOG = LoggerFactory.getLogger(getClass());

    private Path applicationPath;

    public ResourceReader(Path applicationPath) {
        this.applicationPath = applicationPath;
    }

    public Resource getResource(String url) {
        String resourceURL = url.startsWith("/") ? url.substring(1) : url;
        Path resourcePath = applicationPath.resolve(resourceURL);
        Resource resource = new Resource();
        resource.setResourcePath(resourcePath);

        try {
            resource.setContentType(Files.probeContentType(resourcePath));
            resource.setContentLength(Files.size(resourcePath));
            resource.setContent(Files.newInputStream(resourcePath));

            return resource;
        } catch (IOException e) {
            LOG.warn("Static resource not found by path: {}", resourcePath, e);
            throw new WebServerException("Resource not found", HttpStatus.NOT_FOUND);
        }
    }
}
