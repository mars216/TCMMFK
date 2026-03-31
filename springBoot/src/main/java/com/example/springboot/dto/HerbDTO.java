
package com.example.springboot.dto;

import lombok.Data;

import java.util.List;

@Data
public class HerbDTO {
    private String id;
    private String name;
    private String herb_pinyin_name;
    private String herb_en_name;
    private String function;
    private String indications;
    private String usage;
    private String source;
    private String introduction;
    private String modern_research;
    private String property;

    private List<String> taste;
    private List<String> channel_tropism;

    public HerbDTO(String id,String name,String herb_pinyin_name,String herb_en_name) {
        this.name = name;
        this.id=id;
        this.herb_pinyin_name=herb_pinyin_name;
        this.herb_en_name=herb_en_name;
    }
    public HerbDTO(String id,String name) {
        this.name = name;
        this.id=id;
    }
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}
