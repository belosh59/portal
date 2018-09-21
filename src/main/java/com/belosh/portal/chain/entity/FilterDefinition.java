package com.belosh.portal.chain.entity;

public class FilterDefinition {
    private String filterName;
    private String filterDescription;
    private String filterClass;
    private String urlPattern;

    public String getFilterName() {
        return filterName;
    }

    public void setFilterName(String filterName) {
        this.filterName = filterName;
    }

    public String getFilterClass() {
        return filterClass;
    }

    public void setFilterClass(String filterClass) {
        this.filterClass = filterClass;
    }

    public String getUrlPattern() {
        return urlPattern;
    }

    public void setUrlPattern(String urlPattern) {
        this.urlPattern = urlPattern;
    }

    @Override
    public String toString() {
        return "ServletDefinition{" +
                "filterName='" + filterName + '\'' +
                ", filterDescription='" + filterDescription + '\'' +
                ", filterClass='" + filterClass + '\'' +
                ", urlPattern='" + urlPattern + '\'' +
                '}';
    }
}
