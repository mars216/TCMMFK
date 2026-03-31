
package com.example.springboot.dto.detailDTO;


import com.example.springboot.dto.DiseaseDTO;
import com.example.springboot.dto.detailDTO.SimpleDTO;
import lombok.Data;

import java.util.List;

@Data
public class DiseaseDetailDTO {
    private DiseaseDTO diseaseDTO;
    private List<SimpleDTO> proteins;
}