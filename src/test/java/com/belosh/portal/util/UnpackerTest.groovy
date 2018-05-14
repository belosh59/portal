package com.belosh.portal.util

import org.junit.Assert
import org.junit.Test

class UnpackerTest {
    @Test
    void testUnpackWAR() {
        String pathToWAR = "/sample.war"
        String resourceDir = System.getProperty("user.dir") + "\\src\\test\\resources"
        String appName = pathToWAR.replace(".war", "")
        String unzippedPath = resourceDir + appName

        File zippedWar = new File(resourceDir + pathToWAR)
        Unpacker.unpackWAR(zippedWar)

        File unzippedWar = new File(unzippedPath)
        Assert.assertTrue(unzippedWar.exists())
        unzippedWar.deleteDir()
    }
}
