package com.belosh.portal.chain;

import com.belosh.portal.chain.entity.UrlPattern;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.*;

public class PortalFilterChain implements FilterChain {
    private Map<UrlPattern, Filter> applicationFilters;
    private Iterator<Filter> iterator;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response) throws IOException, ServletException {
        if (iterator == null) {
            iterator = getMatchedFiltersIterator(request);
        }

        if (iterator.hasNext()) {
            // Propagate current chain state to next filter in order to save the state of processed request/response
            iterator.next().doFilter(request, response, this);
        }
    }

    public void setApplicationFilters(Map<UrlPattern, Filter> applicationFilters) {
        this.applicationFilters = applicationFilters;
    }

    private Iterator<Filter> getMatchedFiltersIterator(ServletRequest request) {
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        String path = httpServletRequest.getRequestURI().substring(httpServletRequest.getContextPath().length());
        List<Filter> filters = new ArrayList<>();

        for (Map.Entry<UrlPattern, Filter> entry : applicationFilters.entrySet()) {
            if (entry.getKey().matches(path)) {
                filters.add(entry.getValue());
            }
        }

        return filters.iterator();
    }


}
