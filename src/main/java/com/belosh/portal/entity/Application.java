package com.belosh.portal.entity;

import javax.servlet.http.HttpServlet;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Application {
    private String applicationName;
    private File applicationFile;
    private Map<String, HttpServlet> urlPatternToServlet = new HashMap<>();

    public Application(String applicationName, File applicationFile) {
        this.applicationName = applicationName;
        this.applicationFile = applicationFile;
    }

    public List<HttpServlet> getServlets() {
        return new ArrayList<>(urlPatternToServlet.values());
    }

    public String getApplicationName() {
        return applicationName;
    }

    public File getApplicationFile() {
        return applicationFile;
    }

    public HttpServlet getServletByResource(String resource) {
        return urlPatternToServlet.get(resource);
    }

    public void mapResourceToServlet(String resource, HttpServlet servlet) {
        urlPatternToServlet.put(resource, servlet);
    }
}
