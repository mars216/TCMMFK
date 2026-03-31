package com.example.springboot.Mapper;

import com.example.springboot.dto.HerbDTO;
import com.example.springboot.dto.detailDTO.HerbDetailDTO;
import com.example.springboot.utils.Neo4jUtils;
import org.neo4j.driver.*;
import org.neo4j.driver.types.Node;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class HerbMapper {

    @Autowired
    private Driver neo4jDriver;

    public List<HerbDTO> findAllHerbs() {
        List<HerbDTO> list = new ArrayList<>();
        try (Session session = neo4jDriver.session()) {
            String cypher = "MATCH (h:Herb) RETURN h.hubu_id AS id, h.herb_cn_name AS name, h.herb_pinyin_name AS herb_pinyin_name, h.herb_en_name AS herb_en_name";
            Result result = session.run(cypher);
            while (result.hasNext()) {
                org.neo4j.driver.Record r = result.next();
                list.add(new HerbDTO(r.get("id").asString(), r.get("name").asString(), r.get("herb_pinyin_name").asString(), r.get("herb_en_name").asString()));
            }
        }
        return list;
    }

    public List<HerbDTO> searchHerbs(String keyword) {
        List<HerbDTO> list = new ArrayList<>();
        try (Session session = neo4jDriver.session()) {
            String cypher = "MATCH (h:Herb) WHERE h.name CONTAINS $kw RETURN elementId(h) AS id, h.name AS name";
            Result result = session.run(cypher, Values.parameters("kw", keyword));
            while (result.hasNext()) {
                org.neo4j.driver.Record r = result.next();
                list.add(new HerbDTO(r.get("id").asString(), r.get("name").asString()));
            }
        }
        return list;
    }

    public HerbDetailDTO findHerbDetailById(String id) {
        HerbDetailDTO dto = new HerbDetailDTO();

        try (Session session = neo4jDriver.session()) {
            String cypher = "MATCH (h:Herb {hubu_id: $id}) " +
                    "OPTIONAL MATCH (h)-[:Herb_to_Ingredient]->(c:Ingredients) " +
                    "RETURN h, " +
                    "collect(DISTINCT {id: c.hubu_id, name: c.name}) AS components";

            Result result = session.run(cypher, Values.parameters("id", id));
            if (result.hasNext()) {
                org.neo4j.driver.Record record = result.next();
                Node h = record.get("h").asNode();

                dto.setId(h.get("hubu_id").asString(""));
                dto.setName(h.get("herb_cn_name").asString(""));
                dto.setHerbPinyinName(h.get("herb_pinyin_name").asString(""));
                dto.setHerbEnName(h.get("herb_en_name").asString(""));
                dto.setFunction(h.get("function").asString(""));
                dto.setIndications(h.get("indications").asString(""));
                dto.setUsage(h.get("usage").asString(""));
                dto.setSource(h.get("source").asString(""));
                dto.setIntroduction(h.get("introduction").asString(""));
                dto.setModernResearch(h.get("modern_research").asString(""));
                dto.setProperty(h.get("property").asString(""));

                try {
                    dto.setTaste(h.get("taste").isNull() ? Collections.emptyList() : h.get("taste").asList(Value::asString));
                } catch (Exception e) {
                    dto.setTaste(Collections.emptyList());
                }

                try {
                    dto.setChannelTropism(h.get("channel_tropism").isNull() ? Collections.emptyList() : h.get("channel_tropism").asList(Value::asString));
                } catch (Exception e) {
                    dto.setChannelTropism(Collections.emptyList());
                }

                dto.setIngredients(record.get("components").asList(value -> {
                    Map<String, String> map = new HashMap<>();
                    map.put("id", value.get("id").asString(""));
                    map.put("name", value.get("name").asString(""));
                    return map;
                }));
            }
        }

        return dto;
    }
}