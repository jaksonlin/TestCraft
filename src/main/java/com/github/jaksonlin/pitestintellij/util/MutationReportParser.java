package com.github.jaksonlin.pitestintellij.util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.module.kotlin.KotlinModule;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class MutationReportParser {

    private static final XmlMapper xmlMapper = new XmlMapper();

    static {
        xmlMapper.registerModule(new KotlinModule.Builder().build());
        // Enable features that might improve performance
        xmlMapper.enable(JsonParser.Feature.AUTO_CLOSE_SOURCE);
        xmlMapper.enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    }

    public static Mutations parseMutationsFromXml(String filePath) throws IOException {
        File file = new File(filePath);
        try (BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(file))) {
            return xmlMapper.readValue(inputStream, Mutations.class);
        }
    }
}

// Data classes for XML structure

