package com.belosh.portal.parser;

import com.belosh.portal.entity.ServletDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class WebXMLParser {
    private static final Logger logger = LoggerFactory.getLogger(WebXMLParser.class);

    public static Map<String, ServletDefinition> parseWebXml(String webXmlPath) {
        Map<String, ServletDefinition> servletNameToServletDefinition = new HashMap<>();

        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document document = dBuilder.parse(new File(webXmlPath));
            document.getDocumentElement().normalize();

            NodeList imports = document.getElementsByTagName("import");
            for (int i = 0; i < imports.getLength(); i++) {
                Node importXML = imports.item(i);
                //Process root
                Element elementBean = (Element) importXML;
                parseWebXml(elementBean.getAttribute("resource"));
            }

            NodeList servlets = document.getElementsByTagName("servlet");
            for (int i = 0; i < servlets.getLength(); i++){
                Node servlet = servlets.item(i);
                if (servlet.getNodeType() == Node.ELEMENT_NODE) {

                    ServletDefinition servletDefinition = new ServletDefinition();
                    //Process root
                    Element elementServlet = (Element) servlet;

                    String servletName = elementServlet.getElementsByTagName("servlet-name").item(0).getTextContent();
                    String servletClass = elementServlet.getElementsByTagName("servlet-class").item(0).getTextContent();
                    servletDefinition.setServletName(servletName);
                    servletDefinition.setServletClass(servletClass);

                    servletNameToServletDefinition.put(servletName, servletDefinition);
                }
            }

            NodeList servletMappings = document.getElementsByTagName("servlet-mapping");
            for (int i = 0; i < servletMappings.getLength(); i++){
                Node servlet = servletMappings.item(i);
                if (servlet.getNodeType() == Node.ELEMENT_NODE) {
                    //Process root
                    Element elementServlet = (Element) servlet;

                    String servletName = elementServlet.getElementsByTagName("servlet-name").item(0).getTextContent();
                    String urlPattern = elementServlet.getElementsByTagName("url-pattern").item(0).getTextContent();

                    ServletDefinition servletDefinition = servletNameToServletDefinition.get(servletName);
                    servletDefinition.setUrlPattern(urlPattern);
                }
            }

            return servletNameToServletDefinition;
        } catch (IOException e) {
            logger.error("Unable to load document");
            throw new RuntimeException(e);
        } catch (SAXException e) {
            logger.error("Unable to parse xml from stream");
            throw new RuntimeException("Unable to parse xml from stream", e);
        } catch (ParserConfigurationException e) {
            logger.error("Cannot configure DOM Parser");
            throw new RuntimeException("Cannot configure DOM Parser", e);
        }
    }
}
