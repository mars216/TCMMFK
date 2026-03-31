
package com.example.springboot.service;

import com.example.springboot.dto.MedicineLevel1DTO;
import com.example.springboot.Mapper.MedicineCategoryMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MedicineCategoryService {

    private final MedicineCategoryMapper mapper;

    public MedicineCategoryService(MedicineCategoryMapper mapper) {
        this.mapper = mapper;
    }

    public List<MedicineLevel1DTO> getCategoryDTOs() {
        return mapper.findAllCategoryDTOs();
    }

    public Object getCategoryDetail(String id) {
        return mapper.findDetailById(id);
    }
}
