package com.example.springboot.Mapper;

import com.example.springboot.dto.PathwayDTO;
import com.example.springboot.dto.detailDTO.PathwayDetailDTO;
import com.example.springboot.dto.detailDTO.SimpleDTO;
import com.example.springboot.utils.Neo4jUtils;
import org.neo4j.driver.*;
import org.neo4j.driver.types.Node;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;

@Component
public class PathwayMapper {

    private final Driver neo4jDriver;

    public PathwayMapper(Driver neo4jDriver) {
        this.neo4jDriver = neo4jDriver;
    }

    /**
     * Query all pathway nodes (without pagination)
     */
    public List<PathwayDTO> findAllPathways() {
        List<PathwayDTO> pathways = new ArrayList<>();
        try (Session session = neo4jDriver.session()) {
            String cypher = "MATCH (p:Pathway) " +
                    "RETURN p.hubu_id AS id, " +
                    "p.name AS name";

            Result result = session.run(cypher);

            while (result.hasNext()) {
                org.neo4j.driver.Record record = result.next();
                PathwayDTO dto = new PathwayDTO(
                        Neo4jUtils.getStringOrNull(record, "id"),
                        Neo4jUtils.getStringOrNull(record, "name")
                );
                pathways.add(dto);
            }
        }
        return pathways;
    }

    public PathwayDetailDTO findPathwayDetailById(String id) {
        PathwayDetailDTO detailDTO = new PathwayDetailDTO();

        try (Session session = neo4jDriver.session()) {
            String cypher = "MATCH (pw:Pathway {hubu_id: $id}) " +
                    "OPTIONAL MATCH (pw)<-[:Protein_to_Pathway]-(p:Protein) " +
                    "RETURN pw, collect(DISTINCT {id: p.hubu_id, name: p.name}) AS proteins";

            org.neo4j.driver.Record record = session.run(cypher, Values.parameters("id", id)).single();

            Node node = record.get("pw").asNode();

            // Build PathwayDTO
            PathwayDTO dto = new PathwayDTO();
            dto.setId(Neo4jUtils.getStringOrNull(node, "hubu_id"));
            dto.setName(Neo4jUtils.getStringOrNull(node, "name"));
            detailDTO.setPathwayDTO(dto);

            // Build proteins list
            List<SimpleDTO> proteinList = new ArrayList<>();
            for (Value item : record.get("proteins").values()) {
                proteinList.add(new SimpleDTO(item.get("id").asString(), item.get("name").asString()));
            }
            detailDTO.setProteins(proteinList);
        }

        return detailDTO;
    }
}