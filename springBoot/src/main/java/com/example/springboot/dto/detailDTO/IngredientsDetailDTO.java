/**
 * 功能
 * 作者：mars
 * 日期：2025/8/319:22
 */
package com.example.springboot.dto.detailDTO;

import com.example.springboot.dto.IngredientsDTO;
import lombok.Data;

import java.util.List;
import java.util.Map;
@Data
public class IngredientsDetailDTO {
    private IngredientsDTO ingredientsDTO;
    private List<SimpleDTO> proteins;
    private List<SimpleDTO> herbs;
}