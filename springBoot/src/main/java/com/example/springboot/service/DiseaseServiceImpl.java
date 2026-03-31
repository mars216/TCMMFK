
package com.example.springboot.service;

import com.example.springboot.dto.DiseaseDTO;
import com.example.springboot.Mapper.DiseaseMapper;
import com.example.springboot.dto.detailDTO.DiseaseDetailDTO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DiseaseServiceImpl {

    private final DiseaseMapper mapper;

    public DiseaseServiceImpl(DiseaseMapper mapper) {
        this.mapper = mapper;
    }


    public List<DiseaseDTO> findAllDiseases() {
        return mapper.findAllDiseases();
    }
    public DiseaseDetailDTO getDiseaseDetailById(String id) {
        return mapper.findDiseaseDetailById(id);
    }
}
