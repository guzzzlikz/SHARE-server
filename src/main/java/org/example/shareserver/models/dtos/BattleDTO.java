package org.example.shareserver.models.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document(collection="battles")
public class BattleDTO {
    @Id
    private String id;
    private String userId;
    private String teamId;
    private String mobId;
    private String pathToPhoto;
}
