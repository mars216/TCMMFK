/**
 * 功能
 * 作者：mars
 * 日期：2025/8/311:38
 */
package com.example.springboot.dto.detailDTO;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class HerbDetailDTO {
    private String id;
    private String name;
    private String herbPinyinName;
    private String herbEnName;
    private String function;
    private String indications;
    private String usage;
    private String source;
    private String introduction;
    private String modernResearch;
    private String property;
    private List<String> taste;
    private List<String> channelTropism;


    private List<Map<String, String>> ingredients;
}
