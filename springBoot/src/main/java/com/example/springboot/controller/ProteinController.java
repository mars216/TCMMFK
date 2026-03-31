
package com.example.springboot.controller;

import com.example.springboot.common.AuthAccess;
import com.example.springboot.common.Result;
import com.example.springboot.dto.ProteinDTO;
import com.example.springboot.dto.detailDTO.ProteinDetailDTO;
import com.example.springboot.service.ProteinServiceImpl;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/protein")
public class ProteinController {

    private final ProteinServiceImpl proteinService;

    public ProteinController(ProteinServiceImpl proteinService) {
        this.proteinService = proteinService;
    }

    @AuthAccess
    @GetMapping("/all")
    public Result getAllProteins() {
        List<ProteinDTO> list = proteinService.findAllProteins();
        return Result.success(list);
    }
    @AuthAccess
    @GetMapping("/detail/{id}")
    public Result getProteinDetail(@PathVariable String id) {
        ProteinDetailDTO detail = proteinService.getProteinDetailById(id);
        if (detail == null || detail.getProteinDTO() == null) {
            return Result.error("Protein information not found");
        }
        return Result.success(detail);
    }
}
