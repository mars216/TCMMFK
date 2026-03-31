
package com.example.springboot.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProteinDTO {
    private String id;                // hubu_id，
    private String name;              // Gene_name，eg：NCOA2
    private String tcmspId;           // tcmsp_id，eg：TAR03276
    private String tcmspName;         // tcmsp_name
    private String geneId;            // Gene_id，eg：10499
    private String geneName;          // Gene_name
    private List<String> geneAlias;   // Gene_alias： ["GRIP1", "KAT13C", ...]
    private String chromosome;        // Chromosome，eg：8
    private List<String> dbXrefs;     // Db_xrefs： OMIM、HGNC、Ensembl
    private String mapLocation;       // Map_location，eg：8q13.3
    private String description;       // Description，eg：nuclear receptor coactivator 2
    private String typeOfGene;        // Type_of_gene：protein-coding
    private String herbId;            // herb_id
    private  String proteinId;
    public ProteinDTO(String id, String name) {
        this.id = id;
        this.name = name;
    }
}
