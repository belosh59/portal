package com.belosh.portal.application.parser;

import com.belosh.portal.chain.entity.FilterDefinition;
import com.belosh.portal.servlet.entity.ServletDefinition;
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
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

public class WebXMLParser {
    private final Logger logger = LoggerFactory.getLogger(WebXMLParser.class);
    private final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();

    public void parseWebXml(Path webXmlPath,
                            Map<String, ServletDefinition> servletToServletDefinition,
                            Map<String, FilterDefinition> filterToServletDefinition) {

        try {
            DocumentBuilder dBuilder = documentBuilderFactory.newDocumentBuilder();
            Document document = dBuilder.parse(webXmlPath.toFile());
            document.getDocumentElement().normalize();

            /*
            Servlet Section
             */
            NodeList servlets = document.getElementsByTagName("servlet");
            processServletDefinitions(servletToServletDefinition, servlets);

            NodeList servletMappings = document.getElementsByTagName("servlet-mapping");
            processServletMappingDefinition(servletToServletDefinition, servletMappings);

            /*
            Filters Section
             */
            NodeList filters = document.getElementsByTagName("filter");
            processFilterDefinition(filterToServletDefinition, filters);

            NodeList filterMappings = document.getElementsByTagName("filter-mapping");
            processFilterMappingDefinition(filterToServletDefinition, filterMappings);
        } catch (IOException e) {
            logger.error("Unable to load document", e);
            throw new RuntimeException("Unable to load document", e);
        } catch (SAXException e) {
            logger.error("Unable to parse xml from stream", e);
            throw new RuntimeException("Unable to parse xml from stream", e);
        } catch (ParserConfigurationException e) {
            logger.error("Cannot configure DOM Parser", e);
            throw new RuntimeException("Cannot configure DOM Parser", e);
        }
    }

    private void processFilterMappingDefinition(Map<String, FilterDefinition> filterToServletDefinition,
                                                NodeList filterMappings) {
        for (int i = 0; i < filterMappings.getLength(); i++){
            Node filter = filterMappings.item(i);
            if (filter.getNodeType() == Node.ELEMENT_NODE) {
                //Process root
                Element elementServlet = (Element) filter;

                String filterName = elementServlet.getElementsByTagName("filter-name").item(0).getTextContent();
                String urlPattern = elementServlet.getElementsByTagName("url-pattern").item(0).getTextContent();

                FilterDefinition filterDefinition = filterToServletDefinition.get(filterName);
                filterDefinition.setUrlPattern(urlPattern);
            }
        }
    }

    private void processFilterDefinition(Map<String, FilterDefinition> filterToServletDefinition,
                                         NodeList filters) {
        for (int i = 0; i < filters.getLength(); i++){
            Node filter = filters.item(i);
            if (filter.getNodeType() == Node.ELEMENT_NODE) {
                FilterDefinition filterDefinition = new FilterDefinition();
                //Process root
                Element elementServlet = (Element) filter;

                String filterName = elementServlet.getElementsByTagName("filter-name").item(0).getTextContent();
                String filterClass = elementServlet.getElementsByTagName("filter-class").item(0).getTextContent();
                filterDefinition.setFilterName(filterName);
                filterDefinition.setFilterClass(filterClass);

                filterToServletDefinition.put(filterName, filterDefinition);
            }
        }
    }

    private void processServletMappingDefinition(Map<String, ServletDefinition> servletToServletDefinition,
                                                 NodeList servletMappings) {
        for (int i = 0; i < servletMappings.getLength(); i++){
            Node servlet = servletMappings.item(i);
            if (servlet.getNodeType() == Node.ELEMENT_NODE) {
                //Process root
                Element elementServlet = (Element) servlet;

                String servletName = elementServlet.getElementsByTagName("servlet-name").item(0).getTextContent();
                String urlPattern = elementServlet.getElementsByTagName("url-pattern").item(0).getTextContent();

                ServletDefinition servletDefinition = servletToServletDefinition.get(servletName);
                servletDefinition.setUrlPattern(urlPattern);
            }
        }
    }

    private void processServletDefinitions(Map<String, ServletDefinition> servletToServletDefinition,
                                           NodeList servlets) {
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

                servletToServletDefinition.put(servletName, servletDefinition);
            }
        }
    }
}