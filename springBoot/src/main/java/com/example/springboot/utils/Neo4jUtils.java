package com.example.springboot.utils;

import org.neo4j.driver.Record;
import org.neo4j.driver.Value;
import org.neo4j.driver.types.Node;
import org.neo4j.driver.types.Type;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Arrays;
import java.util.stream.Collectors;

public class Neo4jUtils {
    // Safely get string (handle null and numeric types)
    public static String getStringOrNull(org.neo4j.driver.Record record, String key) {
        Value value = record.get(key);
        if (value.isNull()) {
            return null;
        }
        // Convert to string if it's a numeric type
        if (value.type().name().equals("INTEGER") || value.type().name().equals("FLOAT")) {
            return getNumberAsStringOrNull(record, key);
        }
        // Convert other types directly to string
        return value.asString();
    }

    // Handle numeric properties
    public static String getNumberAsStringOrNull(org.neo4j.driver.Record record, String key) {
        Value value = record.get(key);
        if (value.isNull()) {
            return null;
        }
        // Convert to string if it's a numeric type
        if (value.type().name().equals("INTEGER") || value.type().name().equals("FLOAT")) {
            return value.asNumber().toString();
        }
        // Convert other types directly to string
        return value.asString();
    }

    // Safely get list (for CAS only, ensure list type)
    public static List<String> getListOrEmpty(org.neo4j.driver.Record record, String key) {
        try {
            Value value = record.get(key);
            if (value.isNull()) {
                return Collections.emptyList();
            }
            // Try to extract as string list (tolerant processing)
            return value.asList(Value::asString);
        } catch (Exception e) {
            // Return empty if parsing fails
            return Collections.emptyList();
        }
    }

    public static String getStringOrNull(Node node, String key) {
        if (node.containsKey(key)) {
            return node.get(key).isNull() ? null : node.get(key).asString();
        }
        return null;
    }

    public static List<String> getListOrEmpty(Node node, String key) {
        if (node.containsKey(key)) {
            return node.get(key).asList(Value::asString);
        }
        return new ArrayList<>();
    }

    // Safely handle semicolon-separated strings (for alias only)
    public static List<String> getSemicolonListOrEmpty(org.neo4j.driver.Record record, String key) {
        String value = getStringOrNull(record, key);
        if (value == null || value.trim().isEmpty()) {
            return Collections.emptyList();
        }
        return Arrays.stream(value.split(";"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }
}