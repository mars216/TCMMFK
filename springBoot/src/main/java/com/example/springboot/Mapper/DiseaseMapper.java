package com.example.springboot.Mapper;

import com.example.springboot.dto.DiseaseDTO;
import com.example.springboot.dto.detailDTO.DiseaseDetailDTO;
import com.example.springboot.dto.detailDTO.SimpleDTO;
import com.example.springboot.utils.Neo4jUtils;
import org.neo4j.driver.*;
import org.neo4j.driver.types.Node;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class DiseaseMapper {

    private final Driver neo4jDriver;

    public DiseaseMapper(Driver neo4jDriver) {
        this.neo4jDriver = neo4jDriver;
    }

    public List<DiseaseDTO> findAllDiseases() {
        List<DiseaseDTO> diseases = new ArrayList<>();
        try (Session session = neo4jDriver.session()) {
            String cypher = "MATCH (d:Disease) WHERE d.id IS NULL " +
                    "RETURN d.hubu_id AS id, " +
                    "d.name AS name, " +
                    "d.tcmsp_id AS tcmspId, " +
                    "d.tcmsp_name AS tcmspName, " +
                    "d.DisGENet_disease_id AS disgenetDiseaseId, " +
                    "d.herb_id AS herbId, " +
                    "d.Disease_type AS diseaseType, " +
                    "d.synonyms AS synonyms";

            Result result = session.run(cypher);

            while (result.hasNext()) {
                org.neo4j.driver.Record record = result.next();
                DiseaseDTO dto = new DiseaseDTO();
                try {
                    dto.setId(Neo4jUtils.getStringOrNull(record, "id"));
                    dto.setName(Neo4jUtils.getStringOrNull(record, "name"));
                    dto.setTcmspId(Neo4jUtils.getStringOrNull(record, "tcmspId"));
                    dto.setTcmspName(Neo4jUtils.getStringOrNull(record, "tcmspName"));
                    dto.setDisgenetDiseaseId(Neo4jUtils.getStringOrNull(record, "disgenetDiseaseId"));
                    dto.setHerbId(Neo4jUtils.getStringOrNull(record, "herbId"));
                    dto.setDiseaseType(Neo4jUtils.getStringOrNull(record, "diseaseType"));

                    try {
                        dto.setSynonyms(Neo4jUtils.getListOrEmpty(record, "synonyms"));
                    } catch (Exception e) {
                        System.err.println("[ERROR] Failed to parse synonyms field: " + e.getMessage());
                    }

                    diseases.add(dto);
                } catch (Exception e) {
                    System.err.println("❌ Failed to parse disease node: " + e.getMessage());
                    try {
                        System.err.println("Node hubu_id: " + record.get("id").toString());
                    } catch (Exception ex) {
                        System.err.println("⚠️ Failed to get id");
                    }
                }
            }
        }
        return diseases;
    }

    public DiseaseDetailDTO findDiseaseDetailById(String id) {
        DiseaseDetailDTO detailDTO = new DiseaseDetailDTO();

        try (Session session = neo4jDriver.session()) {
            String cypher = "MATCH (d:Disease {hubu_id: $id}) " +
                    "OPTIONAL MATCH (d)<-[:Protein_to_Disease]-(p:Protein) " +
                    "RETURN d, collect(DISTINCT {id: p.hubu_id, name: p.name}) AS proteins";

            org.neo4j.driver.Record record = session.run(cypher, Values.parameters("id", id)).single();

            Node node = record.get("d").asNode();

            DiseaseDTO dto = new DiseaseDTO();
            dto.setId(Neo4jUtils.getStringOrNull(node, "hubu_id"));
            dto.setName(Neo4jUtils.getStringOrNull(node, "name"));
            dto.setTcmspId(Neo4jUtils.getStringOrNull(node, "tcmsp_id"));
            dto.setTcmspName(Neo4jUtils.getStringOrNull(node, "tcmsp_name"));
            dto.setDisgenetDiseaseId(Neo4jUtils.getStringOrNull(node, "DisGENet_disease_id"));
            dto.setHerbId(Neo4jUtils.getStringOrNull(node, "herb_id"));
            dto.setDiseaseType(Neo4jUtils.getStringOrNull(node, "Disease_type"));
            dto.setSynonyms(Neo4jUtils.getListOrEmpty(node, "synonyms"));

            detailDTO.setDiseaseDTO(dto);

            List<SimpleDTO> proteinList = new ArrayList<>();
            for (Value item : record.get("proteins").values()) {
                proteinList.add(new SimpleDTO(item.get("id").asString(), item.get("name").asString()));
            }
            detailDTO.setProteins(proteinList);
        }

        return detailDTO;
    }
}