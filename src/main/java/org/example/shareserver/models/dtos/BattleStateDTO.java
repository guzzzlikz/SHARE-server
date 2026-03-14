package org.example.shareserver.models.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BattleStateDTO {
    private String battleId;
    private int playerHp;
    private int enemyHp;
    private List<String> battleLog;
    @JsonProperty("isOver")
    private boolean isOver;
    private boolean win;
}
