
package com.example.springboot.controller;


import com.example.springboot.common.AuthAccess;
import com.example.springboot.dto.MedicineLevel1DTO;
import com.example.springboot.service.MedicineCategoryService;
import com.example.springboot.common.Result;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/medicine-category")
public class MedicineCategoryController {

    private final MedicineCategoryService categoryService;

    public MedicineCategoryController(MedicineCategoryService categoryService) {
        this.categoryService = categoryService;
    }
    @AuthAccess
    @GetMapping("/all")
    public Result getAllCategoryDTOs() {
        List<MedicineLevel1DTO> list = categoryService.getCategoryDTOs();
        return Result.success(list);
    }

    @AuthAccess
    @GetMapping("/detail/{id}")
    public Result getCategoryDetail(@PathVariable String id) {
        Object detail = categoryService.getCategoryDetail(id);
        if (detail != null) {
            return Result.success(detail);
        } else {
            return Result.error("Category not found or data is empty");
        }
    }

}