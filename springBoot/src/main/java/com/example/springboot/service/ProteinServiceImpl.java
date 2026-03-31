
package com.example.springboot.service;

import com.example.springboot.dto.ProteinDTO;
import com.example.springboot.Mapper.ProteinMapper;
import com.example.springboot.dto.detailDTO.ProteinDetailDTO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProteinServiceImpl {

    private final ProteinMapper mapper;

    public ProteinServiceImpl(ProteinMapper mapper) {
        this.mapper = mapper;
    }


    public List<ProteinDTO> findAllProteins() {
        return mapper.findAllProteins();
    }
    public ProteinDetailDTO getProteinDetailById(String id) {
        return mapper.findProteinDetailById(id);
    }
}
