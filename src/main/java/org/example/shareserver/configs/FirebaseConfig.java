package org.example.shareserver.configs;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Slf4j
@Configuration
public class FirebaseConfig {

    /**
     * Path to a service account JSON file on disk (local / legacy).
     * Ignored if FIREBASE_SERVICE_ACCOUNT_JSON is set.
     */
    @Value("${firebase.service.account.path:}")
    private String serviceAccountPath;

    /**
     * Full service account JSON content as a string (preferred for cloud deployments).
     * Set the FIREBASE_SERVICE_ACCOUNT_JSON environment variable on Render/etc.
     */
    @Value("${firebase.service.account.json:}")
    private String serviceAccountJson;

    @PostConstruct
    public void init() throws IOException {
        if (!FirebaseApp.getApps().isEmpty()) return;

        InputStream credentialsStream = null;

        if (!serviceAccountJson.isBlank()) {
            credentialsStream = new ByteArrayInputStream(
                    serviceAccountJson.getBytes(StandardCharsets.UTF_8));
            log.info("FirebaseApp: using JSON from FIREBASE_SERVICE_ACCOUNT_JSON env var");
        } else if (!serviceAccountPath.isBlank()) {
            credentialsStream = new FileInputStream(serviceAccountPath);
            log.info("FirebaseApp: using file at {}", serviceAccountPath);
        } else {
            log.warn("FirebaseApp not initialized — set FIREBASE_SERVICE_ACCOUNT_JSON or firebase.service.account.path");
            return;
        }

        FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(credentialsStream))
                .build();
        FirebaseApp.initializeApp(options);
        log.info("FirebaseApp initialized successfully");
    }
}
