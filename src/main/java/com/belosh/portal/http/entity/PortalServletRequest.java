package com.belosh.portal.http.entity;

import com.belosh.portal.application.entity.Application;
import com.belosh.portal.http.adapter.HttpServletRequestAdapter;
import com.belosh.portal.http.header.HttpMethod;

import javax.servlet.ServletContext;
import javax.servlet.http.Cookie;
import java.io.BufferedReader;
import java.io.IOException;
import java.security.Principal;
import java.util.*;

public class PortalServletRequest extends HttpServletRequestAdapter {
    private String requestURI;
    private String servletPath;
    private Map<String, String> headers = new HashMap<>();
    private Map<String, String> parameters = new HashMap<>();
    private HttpMethod method;
    private String redirectPath;
    private Application application;
    private String contextPath;
    private Cookie[] cookies;
    private Principal principal;

    @Override
    public String getMethod() {
        return method.toString();
    }

    public void setMethod(HttpMethod method) {
        this.method = method;
    }

    @Override
    public String getHeader(String key) {
        return headers.get(key);
    }

    // TODO: refactor to work with override method over enumeration
    public Map<String, String> getHeaderMap() {
        return headers;
    }

    @Override
    public String getParameter(String s) {
        return parameters.get(s);
    }

    @Override
    public String[] getParameterValues(String s) {
        Collection<String> valuesCollection = parameters.values();
        String[] values = new String[valuesCollection.size()];
        return valuesCollection.toArray(values);
    }

    @Override
    public ServletContext getServletContext() {
        return application;
    }

    public Map<String, String> getParameters() {
        return parameters;
    }

    public String getRedirectPath() {
        return redirectPath;
    }

    public void setRedirectPath(String redirectPath) {
        this.redirectPath = redirectPath;
    }

    public void setApplication(Application application) {
        this.application = application;
    }

    @Override
    public String getRequestURI() {
        return requestURI;
    }

    public void setRequestURI(String requestURI) {
        this.requestURI = requestURI;
    }

    public void setServletPath(String resourceURI) {
        this.servletPath = resourceURI;
    }

    @Override
    public String getServletPath() {
        return servletPath;
    }

    @Override
    public BufferedReader getReader() throws IOException {
        return super.getReader();
    }

    public void setContextPath(String contextPath) {
        this.contextPath = contextPath;
    }

    @Override
    public String getContextPath() {
        return contextPath;
    }

    @Override
    public Cookie[] getCookies() {
        return cookies;
    }

    public void addCookies(Cookie[] cookies) {
        this.cookies = cookies;
    }

    @Override
    public Principal getUserPrincipal() {
        return principal;
    }

    public void setPrincipal(Principal principal) {
        this.principal = principal;
    }
}
