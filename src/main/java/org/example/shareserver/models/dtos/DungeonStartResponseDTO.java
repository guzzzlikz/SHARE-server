package org.example.shareserver.models.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.shareserver.models.entities.Enemy;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DungeonStartResponseDTO {
    private String backgroundUrl;
    private List<Enemy> enemies;
    private double centerLat;
    private double centerLng;
}
