
package com.example.springboot.service;

import com.example.springboot.dto.PathwayDTO;
import com.example.springboot.Mapper.PathwayMapper;
import com.example.springboot.dto.detailDTO.PathwayDetailDTO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PathwayServiceImpl {

    private final PathwayMapper mapper;

    public PathwayServiceImpl(PathwayMapper mapper) {
        this.mapper = mapper;
    }


    public List<PathwayDTO> findAllPathways() {
        return mapper.findAllPathways();
    }
    public PathwayDetailDTO getPathwayDetailById(String id) {
        return mapper.findPathwayDetailById(id);
    }
}
