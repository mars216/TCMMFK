
package com.example.springboot.service;

import com.example.springboot.dto.HerbDTO;
import com.example.springboot.Mapper.HerbMapper;
import com.example.springboot.dto.detailDTO.HerbDetailDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HerbServiceImpl implements HerbService {

    @Autowired
    private HerbMapper herbMapper;

    @Override
    public List<HerbDTO> getAllHerbs() {
        return herbMapper.findAllHerbs();
    }

    @Override
    public List<HerbDTO> searchHerbs(String keyword) {
        return herbMapper.searchHerbs(keyword);
    }
    @Override
    public  HerbDetailDTO getHerbDetailById(String id) {
        return herbMapper.findHerbDetailById(id);
    }
}
