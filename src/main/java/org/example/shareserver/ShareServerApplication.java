package org.example.shareserver;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Objects;

@SpringBootApplication
public class ShareServerApplication {

    public static void main(String[] args) {
        Dotenv env = Dotenv.load();
        System.setProperty("spring.mongodb.uri", Objects.requireNonNull(env.get("MONGO.URI")));
        System.setProperty("jwt.secret", Objects.requireNonNull(env.get("JWT.SECRET")));
        SpringApplication.run(ShareServerApplication.class, args);
    }

}
