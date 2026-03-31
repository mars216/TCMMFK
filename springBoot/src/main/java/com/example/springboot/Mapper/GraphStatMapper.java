package com.example.springboot.Mapper;

import org.neo4j.driver.*;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;

@Component
public class GraphStatMapper {

    @Resource
    private Driver neo4jDriver;

    /**
     * Total count of each node type
     */
    public List<Map<String, Object>> countEachNodeType() {
        String[] labels = {"Herb", "Protein", "Ingredients", "Disease", "Pathway", "MedicineLevel1Category", "MedicineLevel2Category"};
        List<Map<String, Object>> resultList = new ArrayList<>();

        try (Session session = neo4jDriver.session()) {
            for (String label : labels) {
                String cypher = "MATCH (n:" + label + ") RETURN count(n) AS count";
                org.neo4j.driver.Record record = session.run(cypher).single();
                Map<String, Object> map = new HashMap<>();
                map.put("label", label);
                map.put("count", record.get("count").asInt());
                resultList.add(map);
            }
        }
        return resultList;
    }

    /**
     * Number of level-2 categories under each level-1 category
     * + Number of herbs under each level-2 category
     */
    public List<Map<String, Object>> getCategoryStatistics() {
        try (Session session = neo4jDriver.session()) {
            // Query the count of level-2 categories under each level-1 category
            String cypher1 = "MATCH (l1:MedicineLevel1Category)-[:HAS_CHILD]->(l2:MedicineLevel2Category) " +
                    "RETURN l1.name AS level1Name, count(DISTINCT l2) AS category2Count";

            Result result1 = session.run(cypher1);
            Map<String, Map<String, Object>> level1Map = new LinkedHashMap<>();

            while (result1.hasNext()) {
                org.neo4j.driver.Record record = result1.next();
                String level1 = record.get("level1Name").asString("");
                int count = record.get("category2Count").asInt();

                Map<String, Object> map = new HashMap<>();
                map.put("level1", level1);
                map.put("category2Count", count);
                map.put("children", new ArrayList<Map<String, Object>>());
                level1Map.put(level1, map);
            }

            // Query the number of herbs under each level-2 category
            String cypher2 = "MATCH (l1:MedicineLevel1Category)-[:HAS_CHILD]->(l2:MedicineLevel2Category) " +
                    "OPTIONAL MATCH (l2)-[:INCLUDE_OF]->(h:Herb) " +
                    "RETURN l1.name AS level1Name, l2.name AS level2Name, count(DISTINCT h) AS herbCount " +
                    "ORDER BY level1Name, level2Name";

            Result result2 = session.run(cypher2);
            while (result2.hasNext()) {
                org.neo4j.driver.Record record = result2.next();
                String level1 = record.get("level1Name").asString("");
                String level2 = record.get("level2Name").asString("");
                int herbCount = record.get("herbCount").asInt();

                Map<String, Object> child = new HashMap<>();
                child.put("level2", level2);
                child.put("herbCount", herbCount);

                if (level1Map.containsKey(level1)) {
                    List<Map<String, Object>> children = (List<Map<String, Object>>) level1Map.get(level1).get("children");
                    children.add(child);
                }
            }

            return new ArrayList<>(level1Map.values());
        }
    }


    /**
     * Statistical count of herb properties (property), tastes (taste),
     * and meridian tropism (channel_tropism)
     */
    public List<Map<String, Object>> countHerbAllProperties() {
        List<Map<String, Object>> list = new ArrayList<>();

        try (Session session = neo4jDriver.session()) {
            // 1. Single-value field: property
            String propertyCypher = "MATCH (h:Herb) " +
                    "RETURN h.property AS value, count(h) AS count";
            Result propertyResult = session.run(propertyCypher);
            while (propertyResult.hasNext()) {
                org.neo4j.driver.Record record = propertyResult.next();
                Map<String, Object> map = new HashMap<>();
                map.put("type", "property");
                map.put("value", record.get("value").asString(""));
                map.put("count", record.get("count").asInt());
                list.add(map);
            }

            // 2. List field: taste
            String tasteCypher = "MATCH (h:Herb) " +
                    "UNWIND h.taste AS value " +
                    "RETURN value, count(*) AS count";
            Result tasteResult = session.run(tasteCypher);
            while (tasteResult.hasNext()) {
                org.neo4j.driver.Record record = tasteResult.next();
                Map<String, Object> map = new HashMap<>();
                map.put("type", "taste");
                map.put("value", record.get("value").asString(""));
                map.put("count", record.get("count").asInt());
                list.add(map);
            }

            // 3. List field: channel_tropism
            String channelCypher = "MATCH (h:Herb) " +
                    "UNWIND h.channel_tropism AS value " +
                    "RETURN value, count(*) AS count";
            Result channelResult = session.run(channelCypher);
            while (channelResult.hasNext()) {
                org.neo4j.driver.Record record = channelResult.next();
                Map<String, Object> map = new HashMap<>();
                map.put("type", "channel_tropism");
                map.put("value", record.get("value").asString(""));
                map.put("count", record.get("count").asInt());
                list.add(map);
            }
        }

        return list;
    }
}