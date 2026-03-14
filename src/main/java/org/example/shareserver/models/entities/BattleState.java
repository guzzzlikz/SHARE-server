package org.example.shareserver.models.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "battle_states")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BattleState {

    @Id
    private String id;

    private String userId;
    private String enemyId;
    private int playerHp;
    private int enemyHp;
    private boolean isOver;
    private boolean win;
    private List<String> log;
}
