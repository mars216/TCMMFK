package com.example.springboot.Mapper;

import com.example.springboot.dto.ProteinDTO;
import com.example.springboot.dto.detailDTO.ProteinDetailDTO;
import com.example.springboot.dto.detailDTO.SimpleDTO;
import com.example.springboot.utils.Neo4jUtils;
import org.neo4j.driver.*;
import org.neo4j.driver.types.Node;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ProteinMapper {

    private final Driver neo4jDriver;

    public ProteinMapper(Driver neo4jDriver) {
        this.neo4jDriver = neo4jDriver;
    }

    /**
     * Query all protein nodes (without pagination), includes field exception handling
     */
    public List<ProteinDTO> findAllProteins() {
        List<ProteinDTO> proteins = new ArrayList<>();
        try (Session session = neo4jDriver.session()) {
            String cypher = "MATCH (p:Protein) WHERE p.herb_id IS NOT NULL OR p.tcmsp_id IS NOT NULL " +
                    "RETURN p.hubu_id AS id, " +
                    "p.name AS name, " +
                    "p.tcmsp_id AS tcmspId, " +
                    "p.tcmsp_name AS tcmspName, " +
                    "p.Gene_id AS geneId, " +
                    "p.Gene_name AS geneName, " +
                    "p.Gene_alias AS geneAlias, " +
                    "p.Chromosome AS chromosome, " +
                    "p.Db_xrefs AS dbXrefs, " +
                    "p.Map_location AS mapLocation, " +
                    "p.Description AS description, " +
                    "p.Type_of_gene AS typeOfGene, " +
                    "p.id AS proteinId, " +
                    "p.herb_id AS herbId";

            Result result = session.run(cypher);

            while (result.hasNext()) {
                org.neo4j.driver.Record record = result.next();
                ProteinDTO dto = new ProteinDTO();

                try {
                    dto.setId(Neo4jUtils.getStringOrNull(record, "id"));
                    dto.setName(Neo4jUtils.getStringOrNull(record, "name"));
                    dto.setTcmspId(Neo4jUtils.getStringOrNull(record, "tcmspId"));
                    dto.setTcmspName(Neo4jUtils.getStringOrNull(record, "tcmspName"));
                    dto.setGeneId(Neo4jUtils.getStringOrNull(record, "geneId"));
                    dto.setGeneName(Neo4jUtils.getStringOrNull(record, "geneName"));
                    dto.setProteinId(Neo4jUtils.getStringOrNull(record, "proteinId"));
                    // Safely handle List type fields
                    try {
                        dto.setGeneAlias(Neo4jUtils.getListOrEmpty(record, "geneAlias"));
                    } catch (Exception e) {
                        System.err.println("[ERROR] Failed to parse geneAlias: " + e.getMessage());
                    }

                    dto.setChromosome(Neo4jUtils.getStringOrNull(record, "chromosome"));

                    try {
                        dto.setDbXrefs(Neo4jUtils.getListOrEmpty(record, "dbXrefs"));
                    } catch (Exception e) {
                        System.err.println("[ERROR] Failed to parse dbXrefs: " + e.getMessage());
                    }

                    dto.setMapLocation(Neo4jUtils.getStringOrNull(record, "mapLocation"));
                    dto.setDescription(Neo4jUtils.getStringOrNull(record, "description"));
                    dto.setTypeOfGene(Neo4jUtils.getStringOrNull(record, "typeOfGene"));
                    dto.setHerbId(Neo4jUtils.getStringOrNull(record, "herbId"));

                    proteins.add(dto);
                } catch (Exception e) {
                    System.err.println("❌ Failed to parse a protein node: " + e.getMessage());
                    try {
                        System.err.println("Node hubu_id: " + record.get("id").toString());
                    } catch (Exception ex) {
                        System.err.println("⚠️ Failed to get id");
                    }
                }
            }
        }
        return proteins;
    }

    public ProteinDetailDTO findProteinDetailById(String id) {
        ProteinDetailDTO detailDTO = new ProteinDetailDTO();

        try (Session session = neo4jDriver.session()) {
            String cypher = "MATCH (p:Protein {hubu_id: $id}) " +
                    "CALL { " +
                    "  WITH p " +
                    "  OPTIONAL MATCH (p)<-[:Ingredient_to_Protein]-(i:Ingredients) " +
                    "  RETURN collect(DISTINCT {id: i.hubu_id, name: i.name})[0..500] AS ingredients " +
                    "} " +
                    "CALL { " +
                    "  WITH p " +
                    "  OPTIONAL MATCH (p)-[:Protein_to_Disease]->(d:Disease) " +
                    "  RETURN collect(DISTINCT {id: d.hubu_id, name: d.name})[0..500] AS diseases " +
                    "} " +
                    "CALL { " +
                    "  WITH p " +
                    "  OPTIONAL MATCH (p)-[:Protein_to_Pathway]->(pw:Pathway) " +
                    "  RETURN collect(DISTINCT {id: pw.hubu_id, name: pw.name})[0..500] AS pathways " +
                    "} " +
                    "RETURN p, ingredients, diseases, pathways";

            Result result = session.run(cypher, Values.parameters("id", id));

            if (result.hasNext()) {
                org.neo4j.driver.Record record = result.next();
                Node node = record.get("p").asNode();

                ProteinDTO dto = new ProteinDTO();
                dto.setId(Neo4jUtils.getStringOrNull(node, "hubu_id"));
                dto.setName(Neo4jUtils.getStringOrNull(node, "name"));
                dto.setTcmspId(Neo4jUtils.getStringOrNull(node, "tcmsp_id"));
                dto.setTcmspName(Neo4jUtils.getStringOrNull(node, "tcmsp_name"));
                dto.setGeneId(Neo4jUtils.getStringOrNull(node, "gene_id"));
                dto.setGeneName(Neo4jUtils.getStringOrNull(node, "gene_name"));
                dto.setChromosome(Neo4jUtils.getStringOrNull(node, "chromosome"));
                dto.setMapLocation(Neo4jUtils.getStringOrNull(node, "map_location"));
                dto.setDescription(Neo4jUtils.getStringOrNull(node, "description"));
                dto.setTypeOfGene(Neo4jUtils.getStringOrNull(node, "type_of_gene"));
                dto.setHerbId(Neo4jUtils.getStringOrNull(node, "herb_id"));
                dto.setProteinId(Neo4jUtils.getStringOrNull(node, "protein_id"));
                dto.setGeneAlias(Neo4jUtils.getListOrEmpty(node, "gene_alias"));
                dto.setDbXrefs(Neo4jUtils.getListOrEmpty(node, "db_xrefs"));

                detailDTO.setProteinDTO(dto);

                // Build relationships
                detailDTO.setIngredients(parseSimpleDTOList(record, "ingredients"));
                detailDTO.setDiseases(parseSimpleDTOList(record, "diseases"));
                detailDTO.setPathways(parseSimpleDTOList(record, "pathways"));
            }
        }

        return detailDTO;
    }

    private List<SimpleDTO> parseSimpleDTOList(org.neo4j.driver.Record record, String key) {
        List<SimpleDTO> list = new ArrayList<>();
        if (record.containsKey(key)) {
            for (Value v : record.get(key).values()) {
                String id = v.get("id").asString("");
                String name = v.get("name").asString("");
                list.add(new SimpleDTO(id, name));
            }
        }
        return list;
    }
}