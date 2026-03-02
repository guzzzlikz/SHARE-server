package org.example.shareserver.models;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ApiResponse {
    private String token;
    private String message;
    private Object data;
}
