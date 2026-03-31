
package com.example.springboot.controller;

import com.example.springboot.common.AuthAccess;

import com.example.springboot.common.Result;
import com.example.springboot.dto.HerbDTO;
import com.example.springboot.dto.detailDTO.HerbDetailDTO;
import com.example.springboot.service.HerbService;
import com.example.springboot.common.AuthAccess;
import com.example.springboot.service.HerbServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/herb")
public class HerbController {

    @Autowired
    private HerbService herbService;

   @AuthAccess
    @GetMapping("/all")
    public Result getAllHerbs() {
        List<HerbDTO> list = herbService.getAllHerbs();
        return Result.success(list);
    }
   @AuthAccess
    @GetMapping("/search")
    public Result searchHerbs(@RequestParam String keyword) {
        List<HerbDTO> list = herbService.searchHerbs(keyword);
        return Result.success(list);
    }
    @AuthAccess
    @GetMapping("/detail/{id}")
    public Result getHerbDetail(@PathVariable String id) {
        HerbDetailDTO dto = herbService.getHerbDetailById(id);
        return Result.success(dto);
    }
}
