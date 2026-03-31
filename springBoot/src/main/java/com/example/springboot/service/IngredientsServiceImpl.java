
package com.example.springboot.service;
import com.example.springboot.Mapper.IngredientsMapper;
import com.example.springboot.dto.IngredientsDTO;
import com.example.springboot.dto.detailDTO.IngredientsDetailDTO;
import com.example.springboot.dto.detailDTO.ProteinDetailDTO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class IngredientsServiceImpl {
    private final IngredientsMapper mapper;


    public IngredientsServiceImpl(IngredientsMapper mapper) {
        this.mapper = mapper;
    }
    public List<IngredientsDTO> findAllIngredientDTOs() {
        return mapper.findAllIngredientDTOs();
    }
    public IngredientsDetailDTO getIngredientDetailById(String id) {
        return mapper.findIngredientDetailById(id);
    }

}
