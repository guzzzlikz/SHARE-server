package org.example.shareserver.models.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ParamsDTO {
    private String title;
    private String description;
    private double price;
    private double longitude;
    private double latitude;
}