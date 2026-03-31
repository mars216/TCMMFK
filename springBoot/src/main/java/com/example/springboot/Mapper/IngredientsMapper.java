package com.example.springboot.Mapper;

import com.example.springboot.dto.IngredientsDTO;
import com.example.springboot.dto.detailDTO.IngredientsDetailDTO;
import com.example.springboot.dto.detailDTO.SimpleDTO;
import com.example.springboot.utils.Neo4jUtils;
import org.neo4j.driver.*;
import org.neo4j.driver.types.Node;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class IngredientsMapper {

    private final Driver neo4jDriver;

    public IngredientsMapper(Driver neo4jDriver) {
        this.neo4jDriver = neo4jDriver;
    }

    /**
     * Query all ingredient basic information
     * @return List of IngredientsDTO
     */
    public List<IngredientsDTO> findAllIngredientDTOs() {
        List<IngredientsDTO> ingredients = new ArrayList<>();
        try (Session session = neo4jDriver.session()) {
            String cypher = "MATCH (i:Ingredients) " +
                    "RETURN i.hubu_id AS id, " +
                    "i.name AS name, " +
                    "i.tcmsp_id AS tcmspId, " +
                    "i.Pharmacological_and_molecular_properties_data AS pharmacologicalData, " +
                    "i.SMILES AS smiles, " +
                    "i.PubChem_CID_Link AS pubChemCIDLink, " +
                    "i.SymMap_id AS symMapId, " +
                    "i.Formula AS formula, " +
                    "i.OB_score AS obScore, " +
                    "i.InChIKey AS inChIKey, " +
                    "i.CAS AS cas, " +
                    "i.ingredient_weight AS ingredientWeight, " +
                    "i.herb_id AS herbId, " +
                    "i.alias AS alias, " +
                    "i.PubChem_id AS pubChemId, " +
                    "i.PubChem_CID AS pubChemCID";

            Result result = session.run(cypher);

            while (result.hasNext()) {
                org.neo4j.driver.Record record = result.next();
                IngredientsDTO dto = new IngredientsDTO(
                        Neo4jUtils.getStringOrNull(record, "id"),
                        Neo4jUtils.getStringOrNull(record, "name")
                );

                // Get CAS directly as a list
                dto.setCas(Neo4jUtils.getListOrEmpty(record, "cas"));
                // Get alias directly as a string
                dto.setAlias(Neo4jUtils.getStringOrNull(record, "alias"));
                // Set other properties directly
                dto.setTcmspId(Neo4jUtils.getStringOrNull(record, "tcmspId"));
                dto.setPharmacologicalAndMolecularPropertiesData(Neo4jUtils.getStringOrNull(record, "pharmacologicalData"));
                dto.setSmiles(Neo4jUtils.getStringOrNull(record, "smiles"));
                dto.setPubChemCIDLink(Neo4jUtils.getStringOrNull(record, "pubChemCIDLink"));
                dto.setSymMapId(Neo4jUtils.getStringOrNull(record, "symMapId"));
                dto.setFormula(Neo4jUtils.getStringOrNull(record, "formula"));
                dto.setObScore(Neo4jUtils.getStringOrNull(record, "obScore"));
                dto.setInChIKey(Neo4jUtils.getStringOrNull(record, "inChIKey"));
                dto.setIngredientWeight(Neo4jUtils.getStringOrNull(record, "ingredientWeight"));
                dto.setHerbId(Neo4jUtils.getStringOrNull(record, "herbId"));
                dto.setPubChemId(Neo4jUtils.getStringOrNull(record, "pubChemId"));
                dto.setPubChemCID(Neo4jUtils.getStringOrNull(record, "pubChemCID"));

                ingredients.add(dto);
            }
        }
        return ingredients;
    }

    /**
     * Query ingredient details by ID (including associated herbs and proteins)
     * @param id Ingredient hubu_id
     * @return IngredientsDetailDTO
     */
    public IngredientsDetailDTO findIngredientDetailById(String id) {
        IngredientsDetailDTO detailDTO = new IngredientsDetailDTO();

        try (Session session = neo4jDriver.session()) {
            String cypher = "MATCH (i:Ingredients {hubu_id: $id}) " +
                    "OPTIONAL MATCH (i)<-[:Herb_to_Ingredient]-(h:Herb) " +
                    "OPTIONAL MATCH (i)-[:Ingredient_to_Protein]->(p:Protein) " +
                    "RETURN i, " +
                    "collect(DISTINCT {id: h.hubu_id, name: h.herb_cn_name}) AS herbs, " +
                    "collect(DISTINCT {id: p.hubu_id, name: p.name}) AS proteins";

            Result result = session.run(cypher, Values.parameters("id", id));

            if (result.hasNext()) {
                org.neo4j.driver.Record record = result.next();
                Node node = record.get("i").asNode();

                IngredientsDTO dto = new IngredientsDTO();
                dto.setId(Neo4jUtils.getStringOrNull(node, "hubu_id"));
                dto.setName(Neo4jUtils.getStringOrNull(node, "name"));
                dto.setTcmspId(Neo4jUtils.getStringOrNull(node, "tcmsp_id"));
                dto.setPharmacologicalAndMolecularPropertiesData(Neo4jUtils.getStringOrNull(node, "pharmacological_and_molecular_properties_data"));
                dto.setSmiles(Neo4jUtils.getStringOrNull(node, "smiles"));
                dto.setPubChemCIDLink(Neo4jUtils.getStringOrNull(node, "pubChemCID_link"));
                dto.setHubuId(Neo4jUtils.getStringOrNull(node, "hubu_id"));
                dto.setSymMapId(Neo4jUtils.getStringOrNull(node, "symmap_id"));
                dto.setFormula(Neo4jUtils.getStringOrNull(node, "formula"));
                dto.setObScore(Neo4jUtils.getStringOrNull(node, "ob_score"));
                dto.setInChIKey(Neo4jUtils.getStringOrNull(node, "inChIKey"));
                dto.setIngredientWeight(Neo4jUtils.getStringOrNull(node, "ingredient_weight"));
                dto.setHerbId(Neo4jUtils.getStringOrNull(node, "herb_id"));
                dto.setAlias(Neo4jUtils.getStringOrNull(node, "alias"));
                dto.setPubChemId(Neo4jUtils.getStringOrNull(node, "pubChem_id"));
                dto.setPubChemCID(Neo4jUtils.getStringOrNull(node, "pubChemCID"));
                dto.setCas(Neo4jUtils.getListOrEmpty(node, "cas"));

                detailDTO.setIngredientsDTO(dto);

                // Parse associated herbs
                List<SimpleDTO> herbs = new ArrayList<>();
                for (Map<String, Object> item : record.get("herbs").asList(Value::asMap)) {
                    String herbId = (String) item.get("id");
                    String herbName = (String) item.get("name");
                    herbs.add(new SimpleDTO(herbId, herbName));
                }

                // Parse associated proteins
                List<SimpleDTO> proteins = new ArrayList<>();
                for (Map<String, Object> item : record.get("proteins").asList(Value::asMap)) {
                    String pid = (String) item.get("id");
                    String pname = (String) item.get("name");
                    proteins.add(new SimpleDTO(pid, pname));
                }

                detailDTO.setHerbs(herbs);
                detailDTO.setProteins(proteins);
            }
        }

        return detailDTO;
    }

}