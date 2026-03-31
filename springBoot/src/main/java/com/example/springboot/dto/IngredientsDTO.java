
package com.example.springboot.dto;

import lombok.Data;
import java.util.List;

@Data
public class IngredientsDTO {
    private String id;
    private String name;
    private String tcmspId;
    private String pharmacologicalAndMolecularPropertiesData;
    private String smiles;
    private String pubChemCIDLink;
    private String hubuId;
    private String symMapId;
    private String formula;
    private String obScore;
    private String inChIKey;
    private List<String> cas;
    private String ingredientWeight;
    private String herbId;
    private String alias;
    private String pubChemId;
    private String pubChemCID;

    public IngredientsDTO(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public IngredientsDTO() {

    }
}