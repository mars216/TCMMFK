
package com.example.springboot.dto;
import lombok.Data;
import java.util.List;

@Data
public class GraphSearchRequest {
    private List<GraphFilterDTO> filters;
    private List<String> returnTypes;

    private Integer limitPerType = 50;
    private Integer maxHops = 3;
}