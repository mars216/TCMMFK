package com.example.springboot.service;

import com.example.springboot.Mapper.GraphStatMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class GraphStatServiceImpl {

    private final GraphStatMapper graphStatMapper;

    public GraphStatServiceImpl(GraphStatMapper graphStatMapper) {
        this.graphStatMapper = graphStatMapper;
    }

    /**
     * Query total node counts for all node types
     */
    public List<Map<String, Object>> getTotalNodeCounts() {
        return graphStatMapper.countEachNodeType();
    }

    /**
     * Query node label statistics for all top-level categories
     */
    public List<Map<String, Object>> getNodeLabelCounts() {
        return graphStatMapper.getCategoryStatistics();
    }

    /**
     * Statistics for herb properties (nature, flavor, meridian tropism)
     */
    public List<Map<String, Object>> getHerbPropertyStats() {
        return graphStatMapper.countHerbAllProperties();
    }
}