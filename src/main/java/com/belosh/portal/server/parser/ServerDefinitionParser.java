package com.belosh.portal.server.parser;

import com.belosh.portal.server.entity.ServerDefinition;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;

public class ServerDefinitionParser {
    public static ServerDefinition parseServerDefinition(String path) {
        Yaml yaml = new Yaml();
        try (InputStream inputStream = ServerDefinitionParser.class.getResourceAsStream(path)) {
            return yaml.loadAs(inputStream, ServerDefinition.class);
        } catch (IOException e) {
            throw new RuntimeException("Unable to process YAML file by path: " + path);
        }
    }
}
