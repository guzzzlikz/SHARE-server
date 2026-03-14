package org.example.shareserver.models.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EnemyDTO {
    private String id;
    private double latitude;
    private double longitude;
    private String pathToGmapsPhoto;
}
