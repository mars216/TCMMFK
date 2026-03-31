
package com.example.springboot.controller;


import com.example.springboot.common.AuthAccess;
import com.example.springboot.common.Result;
import com.example.springboot.service.GraphSearchServiceImpl;
import lombok.var;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/graph")
public class GraphSearchController {

    private final GraphSearchServiceImpl service;

    public GraphSearchController(GraphSearchServiceImpl service) {
        this.service = service;
    }
    @AuthAccess
    @PostMapping("/search")
    public Result search(@RequestBody Map<String, String> payload) {
        String label = payload.get("label");
        String name = payload.get("name");

        Map<String, Object> graph = service.searchByLabelAndName(label, name);
        return Result.success(graph);
    }

   @AuthAccess
    @PostMapping("/search/expand")
    public Result expand(@RequestBody Map<String, Object> payload) {
        String label = (String) payload.get("label");
        String name = (String) payload.get("name");

        @SuppressWarnings("unchecked")
        var related = (java.util.List<Map<String, String>>) payload.getOrDefault("related", java.util.Collections.emptyList());

        Map<String, Object> graph = service.expandExcludingKnown(label, name, related);
        return Result.success(graph);
    }

}
