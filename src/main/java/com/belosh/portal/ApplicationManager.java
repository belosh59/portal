package com.belosh.portal;

import com.belosh.portal.entity.Application;
import com.belosh.portal.entity.ServletDefinition;
import com.belosh.portal.exception.ApplicationInstantiationException;
import com.belosh.portal.parser.WebXMLParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServlet;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class ApplicationManager {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private static final String APP_WEB_XML_PATH = "/WEB-INF/web.xml";
    private static final String APP_META_PATH = "/META-INF";
    private Map<String, Application> applicationMap = new HashMap<>();
    private ServletLoader servletLoader = new ServletLoader();

    public void loadApplication(File applicationFile) {
        String applicationName = applicationFile.getName();
        Application application = new Application(applicationName, applicationFile);
        Map<String, ServletDefinition> servletDefinitionMap = getServletDefinitions(application);
        try {
            validateWebApp(application);
            for (ServletDefinition servletDefinition : servletDefinitionMap.values()) {
                String urlPattern = servletDefinition.getUrlPattern();
                HttpServlet servlet = servletLoader.createServlet(servletDefinition, application);

                application.mapResourceToServlet(urlPattern, servlet);
            }

            applicationMap.put(applicationName, application);
            logger.info("Application loaded: {}", applicationName);
        } catch (Exception e) {
            logger.error("Failed to load application: {}", applicationName);
            logger.error("Cause: {}", e.getMessage());
        }
    }

    public Application getApplication(String ApplicationName) {
        return applicationMap.get(ApplicationName);
    }

    private Map<String, ServletDefinition> getServletDefinitions(Application application) {
        String webXMLPath = application.getApplicationFile().getAbsolutePath() + APP_WEB_XML_PATH;
        return WebXMLParser.parseWebXml(webXMLPath);
    }

    private void validateWebApp(Application application) {
        File metainf = new File(application.getApplicationFile().getAbsolutePath() + APP_META_PATH);
        File webinf = new File(application.getApplicationFile().getAbsolutePath() + APP_WEB_XML_PATH);
        if (!metainf.exists()) {
            String errorMessage = APP_META_PATH + " path not found in Web Application: " + application.getApplicationName();
            logger.error("Application validation failed: {}", application.getApplicationName());
            logger.error(errorMessage);
            throw new ApplicationInstantiationException(errorMessage);
        }
        if (!webinf.exists()) {
            String errorMessage = APP_WEB_XML_PATH + " path not found in Web Application: " + application.getApplicationName();
            logger.error("Application validation failed: {}", application.getApplicationName());
            logger.error(errorMessage);
            throw new ApplicationInstantiationException(errorMessage);
        }
    }
}
