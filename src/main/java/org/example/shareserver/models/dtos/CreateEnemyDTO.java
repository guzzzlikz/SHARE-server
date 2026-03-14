package org.example.shareserver.models.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateEnemyDTO {
    private String name;
    private String pathToPhoto;

    @NotBlank(message = "City is required")
    private String city;

    private double longitude;
    private double latitude;
    private int hp;
    private int damageToEnemy;
    private boolean isBoss;
    private String chestType;
    private boolean dungeonEntrance;
}
