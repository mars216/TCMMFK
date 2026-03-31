
package com.example.springboot.controller;

import com.example.springboot.common.AuthAccess;
import com.example.springboot.dto.ApiResponse;
import com.example.springboot.dto.GraphSearchRequest;
import com.example.springboot.service.GraphSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.List;

@RestController
@RequestMapping("/api/graph/all")
@RequiredArgsConstructor
public class GraphSearchAllController {

    private final GraphSearchService graphSearchService;

    @AuthAccess
    @PostMapping(value = "/search", consumes = "application/json", produces = "application/json")
    public ApiResponse<Map<String, List<Map<String, Object>>>> search(
            @RequestBody GraphSearchRequest req) {
        try {
            Map<String, List<Map<String, Object>>> data = graphSearchService.search(req);
            return ApiResponse.ok(data);
        } catch (IllegalArgumentException e) {
            return ApiResponse.fail("Invalid parameter: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResponse.fail("Query failed: " + e.getMessage());
        }
    }
}
