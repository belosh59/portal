package com.belosh.portal.util;

import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class Unpacker {
    public static void unpackWAR(File application) {
        byte[] buffer = new byte[1024];
        String destinationPath = application.getAbsolutePath().replace(".war", "");

        try (ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(application))) {
            ZipEntry zipEntry = zipInputStream.getNextEntry();

            if (!destinationPath.endsWith("/")) {
                destinationPath = destinationPath.concat("/");
            }

            while (zipEntry != null) {
                String unzippedEntityName = zipEntry.getName();
                File unzippedEntity = new File(destinationPath + unzippedEntityName);

                if (zipEntry.isDirectory()) {
                    unzippedEntity.mkdirs();
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
            e.printStackTrace();
            throw new RuntimeException("Unable to load archive from the InputStream");

        }
    }
}
