package com.belosh.portal.parser

import com.belosh.portal.application.parser.WebXMLParser
import com.belosh.portal.chain.entity.FilterDefinition
import com.belosh.portal.servlet.entity.ServletDefinition
import org.junit.Assert
import org.junit.Test

import java.nio.file.Path
import java.nio.file.Paths

class WebXMLParserTest {
    @Test
    void parseWebXML() {
        Path pathWebXml = Paths.get(".\\src\\test\\resources\\web.xml")
        Map<String, ServletDefinition> servletDefinitionMap = new HashMap<>()
        Map<String, FilterDefinition> filterDefinitionMap = new HashMap<>()

        WebXMLParser.parseWebXml(pathWebXml, servletDefinitionMap, filterDefinitionMap)

        ServletDefinition perfectServlet = servletDefinitionMap.get("PerfectServlet")
        ServletDefinition helloServlet = servletDefinitionMap.get("HelloServlet")
        ServletDefinition anotherServlet = servletDefinitionMap.get("AnotherServlet")

        Assert.assertEquals('PerfectServlet', perfectServlet.getServletName())
        Assert.assertEquals('mypackage.PerfectClass', perfectServlet.getServletClass())
        Assert.assertEquals('/perfect-requestURI', perfectServlet.getUrlPattern())
        Assert.assertNull(perfectServlet.getServletDescription())

        Assert.assertEquals('HelloServlet', helloServlet.getServletName())
        Assert.assertEquals('mypackage.Hello', helloServlet.getServletClass())
        Assert.assertEquals('/hello', helloServlet.getUrlPattern())
        Assert.assertNull(perfectServlet.getServletDescription())

        Assert.assertEquals('AnotherServlet', anotherServlet.getServletName())
        Assert.assertEquals('mypackage.AnotherClass', anotherServlet.getServletClass())
        Assert.assertNull(anotherServlet.getUrlPattern())
        Assert.assertNull(anotherServlet.getServletDescription())


    }
}
