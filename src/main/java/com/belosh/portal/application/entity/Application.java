package com.belosh.portal.application.entity;

import com.belosh.portal.application.adapter.ServletContextAdapter;
import com.belosh.portal.chain.entity.UrlPattern;
import com.belosh.portal.io.entity.Resource;
import com.belosh.portal.io.reader.ResourceReader;

import javax.servlet.Filter;
import javax.servlet.Servlet;
import javax.servlet.http.HttpServlet;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.util.*;

public class Application extends ServletContextAdapter {

    private final String applicationName;
    private final Path applicationPath;
    private ClassLoader classLoader;
    private final Map<String, HttpServlet> urlPatternToServlet = new HashMap<>();
    private final Map<UrlPattern, Filter> applicationFilters = new HashMap<>();
    private final Map<String, Object> attributes = new HashMap<>();
    private final ResourceReader resourceReader;

    public Application(String applicationName, Path applicationPath) {
        this.applicationName = applicationName;
        this.applicationPath = applicationPath;
        resourceReader = new ResourceReader(applicationPath);
    }

    public String getApplicationName() {
        return applicationName;
    }

    public Path getApplicationPath() {
        return applicationPath;
    }

    public void mapUrlToServlet(String resource, HttpServlet servlet) {
        urlPatternToServlet.put(resource, servlet);
    }

    public void setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public Resource getResourceByUrl(String url) {
        return resourceReader.getResource(url);
    }

    public void addFilter(UrlPattern urlPattern, Filter filter) {
        applicationFilters.put(urlPattern, filter);
    }

    public Map<UrlPattern, Filter> getApplicationFilters() {
        return applicationFilters;
    }

    public int availableFilters() {
        return applicationFilters.size();
    }

    @Override
    public ClassLoader getClassLoader() {
        return classLoader;
    }

    @Override
    public String getContextPath() {
        return applicationPath.normalize().toAbsolutePath().toString();
    }

    public Collection<HttpServlet> getAllServlets() {
        return urlPatternToServlet.values();
    }

    @Override
    public InputStream getResourceAsStream(String s) {
        return resourceReader.getResource(s).getContent();
    }

    @Override
    public URL getResource(String s) throws MalformedURLException {
        return getResourceByUrl(s).getResourcePath().toUri().toURL();
    }

    @Override
    public void setAttribute(String s, Object o) {
        attributes.put(s, o);
    }

    @Override
    public Object getAttribute(String s) {
        return attributes.get(s);
    }

    @Override
    public void removeAttribute(String s) {
        attributes.remove(s);
    }

    @Override
    public Enumeration<String> getAttributeNames() {
        return Collections.enumeration(attributes.keySet());
    }

    @Override
    public Servlet getServlet(String uri) {
        String matchedUrlPattern = null;
        int maxPatternLength = 0;

        if (!uri.startsWith("/")) {
            uri = "/" + uri;
        }

        Set<String> urlPatterns = urlPatternToServlet.keySet();
        for (String urlPattern : urlPatterns) {
            int patternLength = urlPattern.length();
            if (uri.startsWith(urlPattern) && maxPatternLength < patternLength) {
                matchedUrlPattern = urlPattern;
                maxPatternLength = patternLength;
            }
        }
        return urlPatternToServlet.get(matchedUrlPattern);
    }
}
