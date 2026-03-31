package com.example.springboot.entity;

import lombok.Data;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;

import java.util.List;

@Data
@Node("Herb")
public class Herb {

    @Id
    private String name;
    private String herb_id;

    private String herb_cn_name;
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
    public Herb() {}

    public Herb(String name) {
        this.name = name;
    }

    public String getName() { return name; }

    public void setName(String name) { this.name = name; }
}
