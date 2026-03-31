
package com.example.springboot.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ApiResponse<T> {
    private String code; // "200" or other
    private T data;
    private String msg;

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>("200", data, null);
    }
    public static <T> ApiResponse<T> fail(String msg) {
        return new ApiResponse<>("500", null, msg);
    }
}