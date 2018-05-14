package com.belosh.portal;

import com.belosh.portal.entity.Application;
import com.belosh.portal.entity.ServletDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServlet;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Map;

public class ServletLoader {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private static final String APP_CLASSES_PATH = "/WEB-INF/classes";
    private Map<Application, ClassLoader> classLoaderMap = new HashMap<>();

    public HttpServlet createServlet(ServletDefinition servletDefinition, Application application) {
        try {
            String servletClass = servletDefinition.getServletClass();
            ClassLoader classLoader = getClassLoader(application);
            Class<?> clazz = classLoader.loadClass(servletClass);
            HttpServlet httpServlet = (HttpServlet) clazz.getConstructor().newInstance();
            logger.info("Servlet: " + servletDefinition.getServletName() + " has been created.");

            return httpServlet;
        } catch (InstantiationException | ClassNotFoundException e) {
            logger.error("Cannot create servlet from web-app classes for: " + servletDefinition.getServletName());
            throw new RuntimeException("Cannot create servlet from web-app classes for: " + servletDefinition.getServletName());
        } catch (InvocationTargetException | IllegalAccessException e) {
            logger.error("Unable to invoke servlet constructor for: " + servletDefinition.getServletName());
            throw new RuntimeException("Unable to invoke servlet constructor for: " + servletDefinition.getServletName());
        } catch (NoSuchMethodException e) {
            logger.error("Default constructor not found for servlet: " + servletDefinition.getServletName());
            throw new RuntimeException("Default constructor not found for servlet: " + servletDefinition.getServletName());
        }
    }

    private ClassLoader getClassLoader(Application application) {
        if (classLoaderMap.containsKey(application)) {
            return classLoaderMap.get(application);
        } else {
            String applicationClassesPath = application.getApplicationFile().getAbsolutePath() + APP_CLASSES_PATH;
            try {
                File applicationClasses = new File(applicationClassesPath);

                URL url = applicationClasses.toURI().toURL();
                URL[] urls = new URL[]{url};

                return new URLClassLoader(urls);
            } catch (MalformedURLException e) {
                logger.error("Invalid web-app classes path: " + applicationClassesPath);
                throw new RuntimeException("Invalid web-app classes path: " + applicationClassesPath);
            }
        }
    }
}
