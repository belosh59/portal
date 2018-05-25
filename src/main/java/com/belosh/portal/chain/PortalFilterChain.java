package com.belosh.portal.chain;

import com.belosh.portal.chain.entity.Pattern;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

public class PortalFilterChain implements FilterChain {
    private HttpServletRequest httpServletRequest;
    private HttpServletResponse httpServletResponse;

    private Map<Pattern, Filter> applicationFilters;
    private Iterator<Filter> iterator;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response) throws IOException, ServletException {
        httpServletRequest = (HttpServletRequest) request;
        httpServletResponse = (HttpServletResponse) response;

        if (iterator == null) {
            iterator = getMatchedFiltersIterrator(httpServletRequest);
        }

        if (iterator.hasNext()) {
            iterator.next().doFilter(httpServletRequest, httpServletResponse, this);
        }
    }

    public void setApplicationFilters(Map<Pattern, Filter> applicationFilters) {
        this.applicationFilters = applicationFilters;
    }

    public HttpServletRequest getHttpServletRequest() {
        return httpServletRequest;
    }

    public HttpServletResponse getHttpServletResponse() {
        return httpServletResponse;
    }

    private Iterator<Filter> getMatchedFiltersIterrator(HttpServletRequest request) {
        String path = request.getRequestURI().substring(request.getContextPath().length());
        List<Filter> filters = new ArrayList<>();

        for (Map.Entry<Pattern, Filter> entry : applicationFilters.entrySet()) {
            if (entry.getKey().matches(path)) {
                filters.add(entry.getValue());
            }
        }

        return filters.iterator();
    }


}
