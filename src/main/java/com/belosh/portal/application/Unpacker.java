package com.belosh.portal.application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

class Unpacker {
    private static final Logger logger = LoggerFactory.getLogger(Unpacker.class);
    public static void unpackWAR(Path applicationPath) {
        byte[] buffer = new byte[1024];
        String destinationPath = applicationPath.toString().replace(".war", "");

        try (ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(applicationPath.toString()))) {
            ZipEntry zipEntry = zipInputStream.getNextEntry();

            if (!destinationPath.endsWith("/")) {
                destinationPath = destinationPath.concat("/");
            }

            while (zipEntry != null) {
                String unzippedEntityName = zipEntry.getName();
                File unzippedEntity = new File(destinationPath + unzippedEntityName);

                if (zipEntry.isDirectory()) {
                    if (!unzippedEntity.mkdirs()) {
                        logger.error("Unable to create a directory for application");
                        throw new RuntimeException("Unable to create a directory for application");
                    }
                } else {
                    try (FileOutputStream fileOutputStream = new FileOutputStream(unzippedEntity)) {
                        int length;
                        while ((length = zipInputStream.read(buffer)) > 0) {
                            fileOutputStream.write(buffer, 0, length);
                        }
                    }
                }
                zipEntry = zipInputStream.getNextEntry();
            }
        } catch (IOException e) {
            logger.error("Unable to load archive from the InputStream", e);
            throw new RuntimeException("Unable to load archive from the InputStream", e);
        }
    }
}
