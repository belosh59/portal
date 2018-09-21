package com.belosh.portal.application.loader;

import com.belosh.portal.application.entity.Application;
import com.belosh.portal.chain.entity.FilterDefinition;
import com.belosh.portal.chain.entity.PortalFilterConfig;
import com.belosh.portal.servlet.entity.ServletDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.Filter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class ContextLoader {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private static final String APP_CLASSES_PATH = "WEB-INF/classes";
    private static final String APP_LIB_PATH = "WEB-INF/lib";

    public HttpServlet createServlet(ServletDefinition servletDefinition, Application application) {
        try {
            String servletClass = servletDefinition.getServletClass();

            ClassLoader classLoader = getClassLoader(application);
            Class<?> clazz = classLoader.loadClass(servletClass);

            HttpServlet httpServlet = (HttpServlet) clazz.getConstructor().newInstance();
            httpServlet.init();
            logger.info("Servlet: " + servletDefinition.getServletName() + " has been created for: " + application.getApplicationName());

            return httpServlet;
        } catch (InstantiationException | ClassNotFoundException | InvocationTargetException | IllegalAccessException | NoSuchMethodException e) {
            logger.error("Unable to create context from web-app classes for: " + servletDefinition.getServletName(), e);
            throw new RuntimeException("Unable to create context from web-app classes for: " + servletDefinition.getServletName(), e);
        } catch (ServletException e) {
            logger.error("Unable to execute init() method for servlet: " + servletDefinition.getServletName(), e);
            throw new RuntimeException("Unable to execute init() method for servlet: " + servletDefinition.getServletName(), e);
        }
    }

    public Filter createFilter(FilterDefinition filterDefinition, Application application) {
        String filterName = filterDefinition.getFilterName();
        try {
            String filterClass = filterDefinition.getFilterClass();

            ClassLoader classLoader = getClassLoader(application);
            Class<?> clazz = classLoader.loadClass(filterClass);

            Filter filter = (Filter) clazz.getConstructor().newInstance();

            PortalFilterConfig portalFilterConfig = new PortalFilterConfig(application, filterName);
            filter.init(portalFilterConfig);

            logger.info("Filter: " + filterName + " has been created for: " + application.getApplicationName());
            return filter;
        } catch (InstantiationException | ClassNotFoundException | InvocationTargetException | IllegalAccessException | NoSuchMethodException e) {
            logger.error("Unable to create context from web-app classes for: " + filterName, e);
            throw new RuntimeException("Unable to create context from web-app classes for: " + filterName, e);
        } catch (ServletException e) {
            logger.error("Unable to execute init() method for filter: " + filterName, e);
            throw new RuntimeException("Unable to execute init() method for filter: " + filterName, e);
        }
    }

    private ClassLoader getClassLoader(Application application) {
        ClassLoader classLoader = application.getClassLoader();

        if (classLoader != null) {
            return classLoader;
        } else {
            Path applicationClassesPath = application.getApplicationPath().resolve(APP_CLASSES_PATH);
            Path applicationLibPath = application.getApplicationPath().resolve(APP_LIB_PATH);

            try {
                List<URL> urlList = new ArrayList<>();
                try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(applicationLibPath)) {
                    for (Path libPath : directoryStream) {
                        urlList.add(libPath.toUri().toURL());
                    }
                } catch (IOException e) {
                    logger.error("Unable to load application libs for: " + applicationLibPath, e);
                    throw new RuntimeException("Unable to load application libs for: " + applicationLibPath, e);
                }

                URL urlClasses = applicationClassesPath.toUri().toURL();
                urlList.add(urlClasses);

                int urisCount = urlList.size();
                URL[] urlArray = new URL[urisCount];
                urlArray = urlList.toArray(urlArray);

                URLClassLoader urlClassLoader = new URLClassLoader(urlArray);
                application.setClassLoader(urlClassLoader);

                Thread.currentThread().setContextClassLoader(urlClassLoader);

                return urlClassLoader;
            } catch (MalformedURLException e) {
                logger.error("Invalid web-app classes path: " + applicationLibPath, e);
                throw new RuntimeException("Invalid web-app classes path: " + applicationLibPath, e);
            }
        }
    }
}
