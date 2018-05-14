package com.belosh.portal.parser

import com.belosh.portal.entity.ServerDefinition
import org.junit.Assert
import org.junit.Test

class ServerDefinitionParserTest {

    @Test
    void testParseServerDefinition() {
        String path = "/server.yml"
        ServerDefinitionParser serverDefinitionParser = new ServerDefinitionParser()
        ServerDefinition serverDefinition = serverDefinitionParser.parseServerDefinition(path)

        Assert.assertEquals(serverDefinition.serverPort, 8090)
        Assert.assertEquals(serverDefinition.connectionTimeout, 500)
        Assert.assertEquals(serverDefinition.maxThreads, 73)
        Assert.assertEquals(serverDefinition.minThreads, 27)
        Assert.assertEquals(serverDefinition.idleThreads, 36)
        Assert.assertEquals(serverDefinition.threadTimeout, 30)
        Assert.assertEquals(serverDefinition.cachedPool, true)
        Assert.assertEquals(serverDefinition.unpackWARs, true)
        Assert.assertEquals(serverDefinition.autoDeploy, false)
        Assert.assertEquals(serverDefinition.applicationScannerInterval, 5000)
    }
}
