package com.belosh.portal.http;

import com.belosh.portal.http.adapter.PortalHttpServletRequestAdapter;

import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

public class PortalHttpServletRequest extends PortalHttpServletRequestAdapter {
    private String url;
    private Map<String, String> headers = new HashMap<>();
    private Map<String, String> parameters = new HashMap<>();
    private HttpMethod method;
    private String application;
    private String resource;
    private String redirectPath;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

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
    public Map<String, String> getParameterMap() {
        return parameters;
    }

    public String getApplication() {
        return application;
    }

    public void setApplication(String application) {
        this.application = application;
    }

    public String getResource() {
        return resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

    public String getRedirectPath() {
        return redirectPath;
    }

    public void setRedirectPath(String redirectPath) {
        this.redirectPath = redirectPath;
    }
}
