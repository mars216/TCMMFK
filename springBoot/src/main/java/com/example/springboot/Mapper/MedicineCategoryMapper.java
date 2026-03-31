/**
 * Function
 * Author: mars
 * Date: 2025/8/2 16:59
 */
package com.example.springboot.Mapper;

import com.example.springboot.dto.HerbDTO;
import com.example.springboot.dto.MedicineLevel1DTO;
import com.example.springboot.dto.detailDTO.MedicineLevel1DetailDTO;
import com.example.springboot.dto.detailDTO.MedicineLevel2DetailDTO;
import com.example.springboot.dto.detailDTO.SimpleLevel2DTO;
import org.neo4j.driver.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static org.neo4j.driver.Values.parameters;

@Component
public class MedicineCategoryMapper {

    @Autowired
    private Driver neo4jDriver;

    public List<MedicineLevel1DTO> findAllCategoryDTOs() {
        List<MedicineLevel1DTO> list = new ArrayList<>();
        try (Session session = neo4jDriver.session()) {
            String cypher = "MATCH (m1:MedicineLevel1Category)-[:HAS_CHILD]->(m2:MedicineLevel2Category) " +
                    "RETURN m1.hubu_id AS level1Id, m1.name AS level1Name, m2.hubu_id AS level2Id, m2.name AS level2Name";
            Result result = session.run(cypher);
            while (result.hasNext()) {
                org.neo4j.driver.Record r = result.next();
                list.add(new MedicineLevel1DTO(
                        r.get("level1Id").asString(),
                        r.get("level1Name").asString(),
                        r.get("level2Id").asString(),
                        r.get("level2Name").asString()
                ));
            }
        }
        return list;
    }

    public Object findDetailById(String id) {
        try (Session session = neo4jDriver.session()) {
            // Check if it is a level 1 category
            String checkLevel1 = "MATCH (m:MedicineLevel1Category {hubu_id: $id}) RETURN m";
            if (session.run(checkLevel1, parameters("id", id)).hasNext()) {
                // It is a level 1 category
                String cypher = "MATCH (m1:MedicineLevel1Category {hubu_id: $id})-[:HAS_CHILD]->(m2:MedicineLevel2Category) " +
                        "RETURN m1.hubu_id AS level1Id, m1.name AS level1Name, m2.hubu_id AS level2Id, m2.name AS level2Name";
                Result res = session.run(cypher, parameters("id", id));
                List<SimpleLevel2DTO> children = new ArrayList<>();
                String level1Name = "";
                while (res.hasNext()) {
                    org.neo4j.driver.Record r = res.next();
                    level1Name = r.get("level1Name").asString();
                    children.add(new SimpleLevel2DTO(r.get("level2Id").asString(), r.get("level2Name").asString()));
                }
                return new MedicineLevel1DetailDTO(id, level1Name, children);
            }

            // Check if it is a level 2 category
            String checkLevel2 = "MATCH (m:MedicineLevel2Category {hubu_id: $id}) RETURN m";
            if (session.run(checkLevel2, parameters("id", id)).hasNext()) {
                String cypher = "MATCH (m2:MedicineLevel2Category {hubu_id: $id})-[:INCLUDE_OF]->(h:Herb) " +
                        "RETURN m2.name AS level2Name, h.hubu_id AS herbId, h.herb_cn_name AS herbName, " +
                        "h.herb_pinyin_name AS pinyin, h.herb_en_name AS englishName";
                Result res = session.run(cypher, parameters("id", id));
                List<HerbDTO> herbs = new ArrayList<>();
                String level2Name = "";
                while (res.hasNext()) {
                    org.neo4j.driver.Record r = res.next();
                    level2Name = r.get("level2Name").asString();
                    herbs.add(new HerbDTO(
                            r.get("herbId").asString(),
                            r.get("herbName").asString(),
                            r.get("pinyin").asString(),
                            r.get("englishName").asString()
                    ));
                }
                return new MedicineLevel2DetailDTO(id, level2Name, herbs);
            }
        }

        return null;
    }

}