package org.example.shareserver.models.dtos;

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
    private boolean isOver;
    private boolean win;
}
