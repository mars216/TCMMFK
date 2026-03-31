
package com.example.springboot.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DiseaseDTO {
    private String id;                 // id
    private String name;              // name，
    private String tcmspId;           // tcmsp_id，
    private String tcmspName;         // tcmsp_name，
    private String disgenetDiseaseId; // DisGENet_disease_id，
    private String herbId;            // herb_id，
    private String diseaseType;       // Disease_type，
    private List<String> synonyms;    // synonyms，ICD

    public DiseaseDTO(String id, String name) {
        this.id = id;
        this.name = name;
    }
}
