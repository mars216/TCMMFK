
package com.example.springboot.repository;

import com.example.springboot.entity.Herb;
import org.springframework.data.neo4j.repository.Neo4jRepository;

import java.util.List;

public interface HerbRepository extends Neo4jRepository<Herb, String> {
    List<Herb> findByNameContaining(String name);
    List<Herb> findAll();
}
