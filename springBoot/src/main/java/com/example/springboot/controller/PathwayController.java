
package com.example.springboot.controller;

import com.example.springboot.common.AuthAccess;
import com.example.springboot.common.Result;
import com.example.springboot.dto.PathwayDTO;
import com.example.springboot.dto.detailDTO.PathwayDetailDTO;
import com.example.springboot.service.PathwayServiceImpl;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/pathway")
public class PathwayController {

    private final PathwayServiceImpl pathwayService;

    public PathwayController(PathwayServiceImpl pathwayService) {
        this.pathwayService = pathwayService;
    }

    @AuthAccess
    @GetMapping("/all")
    public Result getAllPathways() {
        List<PathwayDTO> list = pathwayService.findAllPathways();
        return Result.success(list);
    }
    @AuthAccess
    @GetMapping("/detail/{id}")
    public Result getPathwayDetail(@PathVariable String id) {
        PathwayDetailDTO detail = pathwayService.getPathwayDetailById(id);
        if (detail == null || detail.getPathwayDTO() == null) {
            return Result.error("The information for this pathway was not found");
        }
        return Result.success(detail);
    }
}
