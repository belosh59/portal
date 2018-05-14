package com.belosh.portal.parser

import com.belosh.portal.entity.ServletDefinition
import org.junit.Assert
import org.junit.Test

class WebXMLParserTest {
    @Test
    void parseWebXML() {
        Map<String, ServletDefinition> servletDefinitionMap = WebXMLParser.parseWebXml("web.xml")

        ServletDefinition perfectServlet = servletDefinitionMap.get("PerfectServlet")
        ServletDefinition helloServlet = servletDefinitionMap.get("HelloServlet")
        ServletDefinition anotherServlet = servletDefinitionMap.get("AnotherServlet")

        Assert.assertEquals('PerfectServlet', perfectServlet.getServletName())
        Assert.assertEquals('mypackage.PerfectClass', perfectServlet.getServletClass())
        Assert.assertEquals('/perfect-url', perfectServlet.getUrlPattern())
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
