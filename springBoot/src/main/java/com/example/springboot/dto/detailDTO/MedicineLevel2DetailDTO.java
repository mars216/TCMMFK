/**
 * 功能
 * 作者：mars
 * 日期：2025/8/310:43
 */
package com.example.springboot.dto.detailDTO;

import com.example.springboot.dto.HerbDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MedicineLevel2DetailDTO {
    private String id;
    private String name;
    private List<HerbDTO> herbs;
}