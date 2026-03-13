package org.example.shareserver;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.Objects;

@SpringBootApplication
@EnableScheduling
public class ShareServerApplication {
    public static void main(String[] args) {
        Dotenv env = Dotenv.load();
        System.setProperty("spring.mongodb.uri", Objects.requireNonNull(env.get("MONGO.URI")));
        System.setProperty("jwt.secret", Objects.requireNonNull(env.get("JWT.SECRET")));
        System.setProperty("ai.key", Objects.requireNonNull(env.get("AI.KEY")));
        System.setProperty("spring.cloud.gcp.credentials.json-key", Objects.requireNonNull(env.get("GCS.KEY")));
        SpringApplication.run(ShareServerApplication.class, args);
    }
}
