package com.example.springboot.Mapper;

import org.neo4j.driver.*;
import org.neo4j.driver.types.Node;
import org.neo4j.driver.types.Relationship;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;

@Component
public class GraphSearchMapper {

    @Resource
    private Driver neo4jDriver;

    public Map<String, Object> findDirectNeighbors(String label, String name) {
        // Whitelist validation to prevent Cypher injection
        Set<String> validLabels = new HashSet<>(Arrays.asList(
                "MedicineLevel1Category",
                "MedicineLevel2Category",
                "Herb",
                "Ingredients",
                "Protein",
                "Disease",
                "Pathway"
        ));

        if (!validLabels.contains(label)) {
            throw new IllegalArgumentException("Invalid label: " + label);
        }

        Map<String, Object> result = new HashMap<>();
        Set<String> nodeIds = new HashSet<>();
        List<Map<String, Object>> nodes = new ArrayList<>();
        List<Map<String, Object>> edges = new ArrayList<>();

        try (Session session = neo4jDriver.session()) {
            // Execute different queries based on label type
            switch (label) {
                case "MedicineLevel1Category":
                    queryMedicineLevel1Category(session, name, nodes, edges, nodeIds);
                    break;
                case "MedicineLevel2Category":
                    queryMedicineLevel2Category(session, name, nodes, edges, nodeIds);
                    break;
                case "Herb":
                    queryHerb(session, name, nodes, edges, nodeIds);
                    break;
                case "Ingredients":
                    queryIngredients(session, name, nodes, edges, nodeIds);
                    break;
                case "Protein":
                    queryProtein(session, name, nodes, edges, nodeIds);
                    break;
                case "Disease":
                    queryDisease(session, name, nodes, edges, nodeIds);
                    break;
                case "Pathway":
                    queryPathway(session, name, nodes, edges, nodeIds);
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported label type: " + label);
            }

            result.put("nodes", nodes);
            result.put("edges", edges);
        }

        return result;
    }

    private void queryMedicineLevel1Category(Session session, String name,
                                             List<Map<String, Object>> nodes,
                                             List<Map<String, Object>> edges,
                                             Set<String> nodeIds) {
        String cypher = "MATCH (n:MedicineLevel1Category {name: $name}) " +
                "OPTIONAL MATCH (n)-[r:HAS_CHILD]->(m:MedicineLevel2Category) " +
                "RETURN n, r, m LIMIT 20";

        executeQuery(session, cypher, name, nodes, edges, nodeIds);
    }

    private void queryMedicineLevel2Category(Session session, String name,
                                             List<Map<String, Object>> nodes,
                                             List<Map<String, Object>> edges,
                                             Set<String> nodeIds) {
        String cypher = "MATCH (n:MedicineLevel2Category {name: $name}) " +
                "OPTIONAL MATCH (n)<-[r:HAS_CHILD]-(m:MedicineLevel1Category) " +
                "RETURN n, r, m LIMIT 20 " +
                "UNION " +
                "MATCH (n:MedicineLevel2Category {name: $name}) " +
                "OPTIONAL MATCH (n)-[r:INCLUDE_OF]->(m:Herb) " +
                "RETURN n, r, m LIMIT 20";

        executeQuery(session, cypher, name, nodes, edges, nodeIds);
    }

    private void queryHerb(Session session, String name,
                           List<Map<String, Object>> nodes,
                           List<Map<String, Object>> edges,
                           Set<String> nodeIds) {
        String cypher = "MATCH (n:Herb {herb_cn_name: $name}) " +
                "OPTIONAL MATCH (n)<-[r:INCLUDE_OF]-(m:MedicineLevel2Category) " +
                "RETURN n, r, m LIMIT 20 " +
                "UNION " +
                "MATCH (n:Herb {herb_cn_name: $name}) " +
                "OPTIONAL MATCH (n)-[r:Herb_to_Ingredient]->(m:Ingredients) " +
                "RETURN n, r, m LIMIT 20";

        executeQuery(session, cypher, name, nodes, edges, nodeIds);
    }

    private void queryIngredients(Session session, String name,
                                  List<Map<String, Object>> nodes,
                                  List<Map<String, Object>> edges,
                                  Set<String> nodeIds) {
        String cypher = "MATCH (n:Ingredients {name: $name}) " +
                "OPTIONAL MATCH (n)<-[r:Herb_to_Ingredient]-(m:Herb) " +
                "RETURN n, r, m LIMIT 20 " +
                "UNION " +
                "MATCH (n:Ingredients {name: $name}) " +
                "OPTIONAL MATCH (n)-[r:Ingredient_to_Protein]->(m:Protein) " +
                "RETURN n, r, m LIMIT 20";

        executeQuery(session, cypher, name, nodes, edges, nodeIds);
    }

    private void queryProtein(Session session, String name,
                              List<Map<String, Object>> nodes,
                              List<Map<String, Object>> edges,
                              Set<String> nodeIds) {
        String cypher = "MATCH (n:Protein {id: $name}) " +
                "OPTIONAL MATCH (n)<-[r:Ingredient_to_Protein]-(m:Ingredients) " +
                "RETURN n, r, m LIMIT 20 " +
                "UNION " +
                "MATCH (n:Protein {id: $name}) " +
                "OPTIONAL MATCH (n)-[r:Protein_to_Disease]->(m:Disease) " +
                "RETURN n, r, m LIMIT 20 " +
                "UNION " +
                "MATCH (n:Protein {id: $name}) " +
                "OPTIONAL MATCH (n)-[r:Protein_to_Pathway]->(m:Pathway) " +
                "RETURN n, r, m LIMIT 20";

        executeQuery(session, cypher, name, nodes, edges, nodeIds);
    }

    private void queryDisease(Session session, String name,
                              List<Map<String, Object>> nodes,
                              List<Map<String, Object>> edges,
                              Set<String> nodeIds) {
        String cypher = "MATCH (n:Disease {name: $name}) " +
                "OPTIONAL MATCH (n)<-[r:Protein_to_Disease]-(m:Protein) " +
                "RETURN n, r, m LIMIT 20";

        executeQuery(session, cypher, name, nodes, edges, nodeIds);
    }

    private void queryPathway(Session session, String name,
                              List<Map<String, Object>> nodes,
                              List<Map<String, Object>> edges,
                              Set<String> nodeIds) {
        String cypher = "MATCH (n:Pathway {name: $name}) " +
                "OPTIONAL MATCH (n)<-[r:Protein_to_Pathway]-(m:Protein) " +
                "RETURN n, r, m LIMIT 20";

        executeQuery(session, cypher, name, nodes, edges, nodeIds);
    }

    private void executeQuery(Session session, String cypher, String name,
                              List<Map<String, Object>> nodes,
                              List<Map<String, Object>> edges,
                              Set<String> nodeIds) {
        Map<String, Object> params = Collections.singletonMap("name", name);
        Result queryResult = session.run(cypher, params);

        while (queryResult.hasNext()) {
            org.neo4j.driver.Record record = queryResult.next();

            // Process the central node
            if (record.containsKey("n") && "NODE".equals(record.get("n").type().name())) {
                Node n = record.get("n").asNode();
                // Use hubu_id or internal ID as node identifier
                String nid = n.containsKey("hubu_id") ? n.get("hubu_id").asString() : String.valueOf(n.id());
                if (!nodeIds.contains(nid)) {
                    nodes.add(buildNodeData(n));
                    nodeIds.add(nid);
                }
            }

            // Process relationship and adjacent nodes
            if (record.containsKey("r") && record.get("r") != null && "RELATIONSHIP".equals(record.get("r").type().name())) {
                Relationship r = record.get("r").asRelationship();

                // Get hubu_id or internal ID for start and end nodes
                Node startNode = record.get("n").asNode(); // Start node
                Node endNode = record.get("m").asNode();   // End node

                String from = startNode.containsKey("hubu_id") ?
                        startNode.get("hubu_id").asString() : String.valueOf(startNode.id());
                String to = endNode.containsKey("hubu_id") ?
                        endNode.get("hubu_id").asString() : String.valueOf(endNode.id());

                Map<String, Object> edge = new HashMap<>();
                Map<String, Object> edgeData = new HashMap<>();
                edgeData.put("source", from);
                edgeData.put("target", to);
                edgeData.put("label", r.type());
                edge.put("data", edgeData);
                edges.add(edge);

                // Also process the adjacent node
                if (record.containsKey("m") && "NODE".equals(record.get("m").type().name())) {
                    Node m = record.get("m").asNode();
                    // Use hubu_id or internal ID as node identifier
                    String mid = m.containsKey("hubu_id") ? m.get("hubu_id").asString() : String.valueOf(m.id());
                    if (!nodeIds.contains(mid)) {
                        nodes.add(buildNodeData(m));
                        nodeIds.add(mid);
                    }
                }
            }
        }
    }

    private Map<String, Object> buildNodeData(Node node) {
        // Prefer hubu_id as ID
        String id = node.containsKey("hubu_id") ? node.get("hubu_id").asString() : String.valueOf(node.id());
        String hubuId = node.containsKey("hubu_id") ? node.get("hubu_id").asString() : "";

        String name = node.containsKey("name")
                ? node.get("name").asString("")
                : (node.containsKey("herb_cn_name") ? node.get("herb_cn_name").asString("") : "");

        String label = node.labels().iterator().hasNext()
                ? node.labels().iterator().next()
                : "Unknown";

        Map<String, Object> data = new HashMap<>();
        data.put("id", id);
        data.put("label", label);
        data.put("hubu_id", hubuId);
        data.put("name", name);

        switch (label) {
            case "Herb":
                data.put("latin_name", getString(node, "herb_latin_name"));
                data.put("property", getString(node, "property"));
                data.put("channel_tropism", getList(node, "channel_tropism"));
                data.put("source", getString(node, "source"));
                break;

            case "Ingredients":
                data.put("Formula", getString(node, "Formula"));
                data.put("SMILES", getString(node, "SMILES"));
                data.put("CAS", getList(node, "CAS"));
                break;

            case "Protein":
                data.put("uniprot_id", getString(node, "id"));
                break;

            case "Disease":
                data.put("description", getString(node, "description"));
                data.put("synonyms", getList(node, "synonyms"));
                break;

            case "MedicineLevel1Category":
            case "MedicineLevel2Category":
                // Only keep name and hubu_id
                break;

            default:
                // Fallback
                data.put("raw_labels", node.labels());
        }

        Map<String, Object> wrapper = new HashMap<>();
        wrapper.put("data", data);
        return wrapper;
    }

    private String getString(Node node, String key) {
        if (!node.containsKey(key)) {
            return "";
        }
        Value value = node.get(key);
        String type = value.type().name();

        switch (type) {
            case "STRING":
                return value.asString("");

            case "LIST":
                // Convert list to comma-separated string
                List<Object> list = value.asList();
                return String.join(", ", list.stream().map(Object::toString).toArray(String[]::new));

            default:
                return value.toString();
        }
    }

    private List<String> getList(Node node, String key) {
        if (!node.containsKey(key)) {
            return new ArrayList<>();
        }
        Value value = node.get(key);
        String type = value.type().name();

        // Directly handle list
        if ("LIST".equals(type)) {
            return value.asList(Value::asString);
        }

        // If string, try splitting by comma
        if ("STRING".equals(type)) {
            String raw = value.asString("");
            if (raw.trim().isEmpty()) {
                return new ArrayList<>();
            }
            return Arrays.asList(raw.split("\\s*,\\s*"));
        }

        return new ArrayList<>();
    }
}