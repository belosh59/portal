package com.belosh.portal;

import com.belosh.portal.util.Unpacker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ApplicationScanner implements Runnable {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private List<String> processedApplications = new ArrayList<>();
    private List<String> unzippedWARs = new ArrayList<>();
    private ApplicationManager applicationManager;
    private int applicationScannerInterval;

    @Override
    public void run() {
        scanFileStorage();
    }

    private void scanFileStorage() {
        File webapps = new File(System.getProperty("user.dir") + "/webapps");
        while (true) {
            try {
                File[] applications = webapps.listFiles();
                if (applications == null) {
                    throw new RuntimeException("Unable to get list of applications. Ensure that server points to webapps folder");
                }
                for (File application : applications) {
                    processApplication(application);
                }
                Thread.sleep(applicationScannerInterval);
            } catch (InterruptedException e) {
                throw new RuntimeException("FileScanner thread was interrupted");
            }

        }
    }

    private void processApplication(File application) {
        String applicationName = application.getName();
        if (!processedApplications.contains(applicationName) && !unzippedWARs.contains(applicationName)) {
           if (application.isDirectory()) {
                logger.info("New application found, starting instantiation: " + applicationName);
                applicationManager.loadApplication(application);
                processedApplications.add(applicationName);
           } else if (applicationName.endsWith(".war")) {
               String targetApplicationName = applicationName.replace(".war", "");
               if (!processedApplications.contains(targetApplicationName)) {
                   Unpacker.unpackWAR(application);
                   logger.info("Application war file unpacked: " + applicationName);
               } else {
                   unzippedWARs.add(applicationName);
                   logger.warn("Application with the same name already registered. WAR achieve will not be unpacked for: " + applicationName);
               }

           }
        }
    }

    public void setApplicationManager(ApplicationManager applicationManager) {
        this.applicationManager = applicationManager;
    }

    public void setApplicationScannerInterval(int applicationScannerInterval) {
        this.applicationScannerInterval = applicationScannerInterval;
    }
}
