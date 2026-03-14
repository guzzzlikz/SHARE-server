package org.example.shareserver.models.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "enemies")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Enemy {
    @Id
    private String id;
    private String name;
    private String pathToPhoto;
    private String pathToGmapsPhoto;
    private String city;
    private double longitude;
    private double latitude;
    private int hp;
    private int damage;
    private Instant killedAt;
}
