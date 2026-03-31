/**
 * 功能
 * 作者：mars
 * 日期：2025/8/319:50
 */
package com.example.springboot.dto.detailDTO;

import com.example.springboot.dto.ProteinDTO;
import lombok.Data;

import java.util.List;

@Data
public class ProteinDetailDTO {
    private ProteinDTO proteinDTO;
    private List<SimpleDTO> ingredients;
    private List<SimpleDTO> diseases;
    private List<SimpleDTO> pathways;
}
