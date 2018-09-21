package com.belosh.portal.application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

public class ApplicationScanner implements Runnable {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private static final String DEFAULT_WEBAPP_PATH = "./webapps";
    private static final String ARCHIVE_EXTENSION = ".war";
    private final List<String> processedApplications = new ArrayList<>();
    private final List<String> unzippedWARs = new ArrayList<>();
    private final ApplicationManager applicationManager;
    private boolean autoDeploy = true;
    private boolean unpackWARs = true;

    public ApplicationScanner(ApplicationManager applicationManager) {
        this.applicationManager = applicationManager;
    }

    @Override
    public void run() {
        Thread.currentThread().setName("application-scanner-thread");
        scanWebApps();
    }

    private void scanWebApps() {
        Path webapps = Paths.get(DEFAULT_WEBAPP_PATH);

        try {
            scanExistingApplications(webapps);
            if (autoDeploy) {
                scanNewApplications(webapps);
            }
        } catch (IOException e) {
            logger.error("Unable to get list of applications", e);
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            logger.error("FileScanner thread interrupted", e);
            throw new RuntimeException(e);
        } catch (Throwable e) {
            logger.error("Uncaught exception: ", e);
        }
    }

    private void processApplication(Path applicationPath) {
        // Validation
        String applicationName = applicationPath.getFileName().toString();
        if (!processedApplications.contains(applicationName) && !unzippedWARs.contains(applicationName)) {
            return;
        }

        if (applicationName.endsWith(ARCHIVE_EXTENSION) && !unpackWARs) {
            logger.warn("Archive: {}, will not be unpacked due to unpackWARs = false in server.yml", applicationName);
            return;
        }

        // Process unpack
        if (Files.isDirectory(applicationPath)) {
            logger.info("New application found, starting deployment: {}", applicationName);
            applicationManager.deployApplication(applicationPath);
            processedApplications.add(applicationName);

        } else if (applicationName.endsWith(ARCHIVE_EXTENSION)) {
            processArchive(applicationPath, applicationName);
        } else {
            logger.warn("Processing skipped. Invalid application: {}" , applicationName);
        }
    }

    private void processArchive(Path applicationPath, String applicationName) {
        String targetApplicationName = applicationName.replace(ARCHIVE_EXTENSION, "");

        logger.info("New archive found: " + applicationName);
        if (!processedApplications.contains(targetApplicationName)) {
           Unpacker.unpackWAR(applicationPath);
           logger.info("WAR archive unpacked: " + applicationName);

           Path unpackedApplication = applicationPath.getParent().resolve(targetApplicationName).normalize();
           processApplication(unpackedApplication);

        } else {
           unzippedWARs.add(applicationName);
           logger.warn("Application with the same name already registered. WAR achieve will not be unpacked for: {}", applicationName);
        }
    }

    private void scanExistingApplications(Path path) throws IOException {
        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(path)) {
            for (Path applicationPath : directoryStream) {
                processApplication(applicationPath);
            }
        }
    }

    private void scanNewApplications(Path path) throws IOException, InterruptedException {
        try (WatchService watchService = FileSystems.getDefault().newWatchService()) {
            path.register(watchService, StandardWatchEventKinds.ENTRY_CREATE);

            WatchKey key;
            while ((key = watchService.take()) != null) {
                for (WatchEvent<?> event : key.pollEvents()) {
                    Path applicationPath = path.resolve(event.context().toString());
                    processApplication(applicationPath);
                }
                key.reset();
            }
        }
    }

    public void setAutoDeploy(boolean autoDeploy) {
        this.autoDeploy = autoDeploy;
    }

    public void setUnpackWARs(boolean unpackWARs) {
        this.unpackWARs = unpackWARs;
    }
}
