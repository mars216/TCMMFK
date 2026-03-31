
package com.example.springboot.service;

import com.example.springboot.dto.HerbDTO;
import com.example.springboot.dto.detailDTO.HerbDetailDTO;

import java.util.List;

public interface HerbService {
    List<HerbDTO> getAllHerbs();
    List<HerbDTO> searchHerbs(String keyword);
    HerbDetailDTO getHerbDetailById(String id);
}