/**
 * 功能
 * 作者：mars
 * 日期：2025/8/310:42
 */
package com.example.springboot.dto.detailDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MedicineLevel1DetailDTO {
    private String id;
    private String name;
    private List<SimpleLevel2DTO> children;
}

