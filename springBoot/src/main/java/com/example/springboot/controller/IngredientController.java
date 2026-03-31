
package com.example.springboot.controller;
import com.example.springboot.common.AuthAccess;
import com.example.springboot.common.Result;
import com.example.springboot.dto.IngredientsDTO;
import com.example.springboot.dto.detailDTO.HerbDetailDTO;
import com.example.springboot.dto.detailDTO.IngredientsDetailDTO;
import com.example.springboot.service.HerbServiceImpl;
import com.example.springboot.service.IngredientsServiceImpl;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/ingredient")
public class IngredientController {

    private final IngredientsServiceImpl ingredientService;

    public IngredientController(IngredientsServiceImpl ingredientService) {
        this.ingredientService = ingredientService;
    }

    @AuthAccess
    @GetMapping("/all")
    public Result getAllIngredients() {
        List<IngredientsDTO> list = ingredientService.findAllIngredientDTOs();
        return Result.success(list);
    }
    @AuthAccess
    @GetMapping("/detail/{id}")
    public Result getIngredientDetail(@PathVariable String id) {
        IngredientsDetailDTO detail = ingredientService.getIngredientDetailById(id);
        if (detail == null || detail.getIngredientsDTO() == null) {
            return Result.error("Information for this ingredient was not found");
        }
        return Result.success(detail);
    }

}
