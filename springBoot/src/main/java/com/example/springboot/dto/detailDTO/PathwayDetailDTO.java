/**
 * 功能
 * 作者：mars
 * 日期：2025/8/320:14
 */
package com.example.springboot.dto.detailDTO;

import com.example.springboot.dto.PathwayDTO;
import com.example.springboot.dto.detailDTO.SimpleDTO;
import lombok.Data;

import java.util.List;

@Data
public class PathwayDetailDTO {
    private PathwayDTO pathwayDTO;
    private List<SimpleDTO> proteins;  // 相关蛋白质（id, name）
}
