package com.belosh.portal.servlet.entity;

public class ServletDefinition {

    private String servletName;
    private String servletDescription;
    private String servletClass;
    private String urlPattern;

    public String getServletName() {
        return servletName;
    }

    public void setServletName(String servletName) {
        this.servletName = servletName;
    }

    public String getServletDescription() {
        return servletDescription;
    }

    public void setServletDescription(String servletDescription) {
        this.servletDescription = servletDescription;
    }

    public String getServletClass() {
        return servletClass;
    }

    public void setServletClass(String servletClass) {
        this.servletClass = servletClass;
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
                "servletName='" + servletName + '\'' +
                ", servletDescription='" + servletDescription + '\'' +
                ", servletClass='" + servletClass + '\'' +
                ", urlPattern='" + urlPattern + '\'' +
                '}';
    }
}
