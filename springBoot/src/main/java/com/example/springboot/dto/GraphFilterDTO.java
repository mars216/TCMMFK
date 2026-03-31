
package com.example.springboot.dto;
import lombok.Data;

@Data
public class GraphFilterDTO {
    private String type;   // Herb / Protein / ...
    private String name;
}