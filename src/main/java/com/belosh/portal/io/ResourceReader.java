package com.belosh.portal.io;


import com.belosh.portal.exception.WebServerException;
import com.belosh.portal.http.StatusCode;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class ResourceReader {
    private String resourcePath;

    public ResourceReader(String resourcePath) {
        this.resourcePath = resourcePath;
    }

    public BufferedInputStream readContent(String url) {
        try {
            BufferedInputStream bufferedInputStream = new BufferedInputStream(
                    new FileInputStream(resourcePath + url));
            return bufferedInputStream;
        } catch (FileNotFoundException e) {
            throw new WebServerException("Requested file " + url + " has not been found in resources.", e, StatusCode.NOT_FOUND);
        }
    }
}
