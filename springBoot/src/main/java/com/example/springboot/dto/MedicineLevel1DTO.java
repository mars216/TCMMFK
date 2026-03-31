
package com.example.springboot.dto;

import lombok.Data;

@Data
public class MedicineLevel1DTO {
    private String level1Id;
    private String level1Name;
    private String level2Id;
    private String level2Name;

    public MedicineLevel1DTO(String level1Id, String level1Name, String level2Id, String level2Name) {
        this.level1Id = level1Id;
        this.level1Name = level1Name;
        this.level2Id = level2Id;
        this.level2Name = level2Name;
    }

    // Getters & Setters
//    public String getLevel1Id() { return level1Id; }
//    public void setLevel1Id(String level1Id) { this.level1Id = level1Id; }
//
//    public String getLevel1Name() { return level1Name; }
//    public void setLevel1Name(String level1Name) { this.level1Name = level1Name; }
//
//    public String getLevel2Id() { return level2Id; }
//    public void setLevel2Id(String level2Id) { this.level2Id = level2Id; }
//
//    public String getLevel2Name() { return level2Name; }
//    public void setLevel2Name(String level2Name) { this.level2Name = level2Name; }
}

