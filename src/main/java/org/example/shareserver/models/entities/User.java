package org.example.shareserver.models.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@Document(collection="users")
public class User {
    @Id
    private String id;
    private String nickname;
    private String email;
    private String pathToPhoto;
    private String password;

    private int hp = 100;
    private int xp = 0;
    private int lvl = 0;
    private int damage = 15;
    private int coins = 200;
    private int gems = 10;
    private List<Item> items;

    private Map<Integer, Integer> lvlMap; // lvl xp

    public User(){
        initMap();
    }

    private void initMap(){
        for (int i = 0; i < 20; i++) {
            lvlMap.put(i + 1, (int) (50 + 50 * Math.pow(1.1, i)));
        }
    }

    public void setXp(int xp){
        this.xp = xp;
        for (int i = 0; i < lvlMap.values().size(); i++) {
            int lvlXp = lvlMap.get(i+1);
            if (xp < lvlXp && lvl < i+1){
                this.lvl = i+1;
                this.hp += 5;
                this.damage += 5;
            }
        }
    }
}
