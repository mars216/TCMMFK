
package com.example.springboot.controller;

import com.example.springboot.common.AuthAccess;
import com.example.springboot.common.Result;
import com.example.springboot.dto.DiseaseDTO;
import com.example.springboot.dto.detailDTO.DiseaseDetailDTO;
import com.example.springboot.service.DiseaseServiceImpl;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/disease")
public class DiseaseController {

    private final DiseaseServiceImpl diseaseService;

    public DiseaseController(DiseaseServiceImpl diseaseService) {
        this.diseaseService = diseaseService;
    }

    @AuthAccess
    @GetMapping("/all")
    public Result getAllDiseases() {
        List<DiseaseDTO> list = diseaseService.findAllDiseases();
        return Result.success(list);
    }
    @AuthAccess
    @GetMapping("/detail/{id}")
    public Result getDiseaseDetail(@PathVariable String id) {
        DiseaseDetailDTO detail = diseaseService.getDiseaseDetailById(id);
        if (detail == null || detail.getDiseaseDTO() == null) {
            return Result.error("Disease information not found");
        }
        return Result.success(detail);
    }
}
