package org.example.shareserver.models.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
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
    private int damage = 15;
    private int coins = 200;
    private int gems = 10;
    private List<Item> items;
}
