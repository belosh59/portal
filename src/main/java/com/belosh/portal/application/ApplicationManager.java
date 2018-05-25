package com.belosh.portal.application;

import com.belosh.portal.chain.PortalFilterChain;
import com.belosh.portal.application.entity.Application;
import com.belosh.portal.chain.entity.FilterDefinition;
import com.belosh.portal.application.loader.ContextLoader;
import com.belosh.portal.chain.entity.Pattern;
import com.belosh.portal.servlet.entity.ServletDefinition;
import com.belosh.portal.exception.ApplicationDeploymentException;
import com.belosh.portal.application.parser.WebXMLParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.Filter;
import javax.servlet.http.HttpServlet;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class ApplicationManager {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private static final String APP_WEB_XML_PATH = "WEB-INF/web.xml";
    private static final String APP_META_PATH = "META-INF";

    private Map<String, Application> applicationMap = new HashMap<>();
    private ContextLoader contextLoader = new ContextLoader();

    public ApplicationManager() {
        Runtime.getRuntime().addShutdownHook(new ShutDownHook());
    }

    public void deployApplication(Path applicationPath) {
        String applicationName = applicationPath.getFileName().toString();
        Application application = new Application(applicationName, applicationPath);
        validateWebApp(application);

        Path webXMLPath = application.getApplicationPath().resolve(APP_WEB_XML_PATH);
        WebXMLParser webXMLParser = new WebXMLParser();
        webXMLParser.parseWebXml(webXMLPath);

        try {
            for (ServletDefinition servletDefinition : webXMLParser.getServletDefinitions()) {
                String urlPattern = servletDefinition.getUrlPattern();
                HttpServlet servlet = contextLoader.createServlet(servletDefinition, application);

                application.mapUrlToServlet(urlPattern, servlet);
            }


            for (FilterDefinition filterDefinition : webXMLParser.getFilterDefinitions()) {
                String urlPattern = filterDefinition.getUrlPattern();
                Pattern pattern = new Pattern(urlPattern);
                Filter filter = contextLoader.createFilter(filterDefinition, application);
                application.addFilter(pattern, filter);
            }

            applicationMap.put(applicationName, application);
            logger.info("Application loaded: {}", applicationName);
        } catch (Throwable e) {
            logger.error("Failed to load application: {}", applicationName, e);
        }
    }

    public Application getApplication(String ApplicationName) {
        return applicationMap.get(ApplicationName);
    }

    private void validateWebApp(Application application) {
        Path metaInf = application.getApplicationPath().resolve(APP_META_PATH);
        Path webInf = application.getApplicationPath().resolve(APP_WEB_XML_PATH);

        for (Path path : new Path[]{metaInf, webInf}) {
            if (!Files.exists(path)) {
                logger.error("{} path not found in Web Application: {}. Validation failed", path.normalize(), application.getApplicationName());
                throw new ApplicationDeploymentException("Application structure validation failed");
            }
        }
    }

    class ShutDownHook extends Thread {
        @Override
        public void run() {
            logger.info("Interrupt signal detected. ShutDownHook processing started:");
            for (Application application : applicationMap.values()) {
                logger.info("Destroying servlets for application: {}", application.getApplicationName());
                for (HttpServlet servlet : application.getAllServlets()) {
                    servlet.destroy();
                }
                logger.info("All servlets destroyed for application: {}", application.getApplicationName());
            }
        }
    }
}
