package com.belosh.portal.chain.entity;

import com.belosh.portal.application.entity.Application;

import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import java.util.Enumeration;

public class PortalFilterConfig implements FilterConfig {
    private ServletContext context;
    private String filterName;

    public PortalFilterConfig(Application application, String filterName) {
        this.context = application;
        this.filterName = filterName;
    }

    @Override
    public String getFilterName() {
        return filterName;
    }

    @Override
    public ServletContext getServletContext() {
        return context;
    }

    // TODO: to be implemented
    @Override
    public String getInitParameter(String s) {
        return null;
    }

    @Override
    public Enumeration<String> getInitParameterNames() {
        return null;
    }
}