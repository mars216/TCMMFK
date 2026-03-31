
package com.example.springboot.controller;

import com.example.springboot.common.AuthAccess;
import com.example.springboot.service.GraphStatServiceImpl;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/stats")
public class GraphStatController {

    private final GraphStatServiceImpl graphStatService;

    public GraphStatController(GraphStatServiceImpl graphStatService) {
        this.graphStatService = graphStatService;
    }
    @AuthAccess
    @GetMapping("/node/total")
    public List<Map<String, Object>> getTotalNodes() {
        return graphStatService.getTotalNodeCounts();
    }
   @AuthAccess
    @GetMapping("/node/labels")
    public List<Map<String, Object>> getNodeLabels() {
        return graphStatService.getNodeLabelCounts();
    }
    @AuthAccess
    @GetMapping("/herb/properties")
    public List<Map<String, Object>> getHerbPropertyStats() {
        return graphStatService.getHerbPropertyStats();
    }
}
