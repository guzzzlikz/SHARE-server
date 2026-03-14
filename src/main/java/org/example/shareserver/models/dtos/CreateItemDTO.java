package org.example.shareserver.models.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateItemDTO {
    private String name;
    private String pathToPhoto;
    private int hp;
    private int damage;
    private boolean equipped;
}
