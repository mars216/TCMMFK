
package com.example.springboot.service;

import com.example.springboot.Mapper.GraphSearchMapper;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class GraphSearchServiceImpl {

    private final GraphSearchMapper mapper;

    public GraphSearchServiceImpl(GraphSearchMapper mapper) {
        this.mapper = mapper;
    }

    public Map<String, Object> searchByLabelAndName(String label, String name) {
        return mapper.findDirectNeighbors(label, name);
    }
    public Map<String, Object> expandExcludingKnown(String label, String name, List<Map<String, String>> related) {
        Map<String, Object> rawResult = mapper.findDirectNeighbors(label, name);

        List<Map<String, Object>> nodes = (List<Map<String, Object>>) rawResult.get("nodes");
        List<Map<String, Object>> edges = (List<Map<String, Object>>) rawResult.get("edges");

        // Remove duplicates by label only
        Set<String> knownLabels = new HashSet<>();
        for (Map<String, String> item : related) {
            knownLabels.add(item.get("label"));
        }
        knownLabels.add(label); // Exclude the label of the current clicked node

        // Filter nodes, exclude nodes whose label is in knownLabels
        List<Map<String, Object>> filteredNodes = new ArrayList<>();
        Set<String> retainedIds = new HashSet<>();

        for (Map<String, Object> node : nodes) {
            Map<String, Object> data = (Map<String, Object>) node.get("data");
            String nodeLabel = (String) data.get("label");
            String nodeId = (String) data.get("id");

            if (!knownLabels.contains(nodeLabel)) {
                filteredNodes.add(node);
                retainedIds.add(nodeId);
            }
        }

        // Keep the current clicked node regardless of whether its label is in the exclusion list
        for (Map<String, Object> node : nodes) {
            Map<String, Object> data = (Map<String, Object>) node.get("data");
            String nodeLabel = (String) data.get("label");
            String nodeName = (String) data.get("name");

            if (label.equals(nodeLabel) && name.equals(nodeName)) {
                filteredNodes.add(node);
                retainedIds.add((String) data.get("id"));
                break;
            }
        }

        // Filter edges, only keep edges whose both ends are in retainedIds
        List<Map<String, Object>> filteredEdges = new ArrayList<>();
        for (Map<String, Object> edge : edges) {
            Map<String, Object> data = (Map<String, Object>) edge.get("data");
            String source = (String) data.get("source");
            String target = (String) data.get("target");

            if (retainedIds.contains(source) && retainedIds.contains(target)) {
                filteredEdges.add(edge);
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("nodes", filteredNodes);
        result.put("edges", filteredEdges);
        return result;
    }


}