package com.belosh.portal.application.hook;

import com.belosh.portal.application.entity.Application;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.Filter;
import javax.servlet.http.HttpServlet;
import java.util.Map;

public class ShutDownHook extends Thread {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final Map<String, Application> applicationMap;

    public ShutDownHook(Map<String, Application> applicationMap) {
        this.applicationMap = applicationMap;
    }

    @Override
    public void run() {
        logger.info("Interrupt signal detected. ShutDownHook processing started:");
        for (Application application : applicationMap.values()) {
            String applicationName = application.getApplicationName();

            logger.info("Destroying servlets for application: {}", applicationName);
            for (HttpServlet servlet : application.getAllServlets()) {
                servlet.destroy();
            }

            logger.info("Destroying filters for application: {}", applicationName);
            for (Filter filter : application.getApplicationFilters().values()) {
                filter.destroy();
            }

            logger.info("All servlets destroyed for application: {}", application.getApplicationName());
        }


    }
}