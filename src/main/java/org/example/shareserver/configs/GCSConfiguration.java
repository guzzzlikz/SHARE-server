package org.example.shareserver.configs;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;

@Configuration
public class GCSConfiguration {

    @Value("${spring.cloud.gcp.credentials.json-key}")
    private String credentialsJson;

    @Bean
    public Storage storage() throws IOException {
        byte[] decodedBytes = Base64.getDecoder().decode(credentialsJson);

        String json = new String(decodedBytes, StandardCharsets.UTF_8)
                .replace("\\n", "\n");

        System.out.println(json);

        GoogleCredentials credentials = GoogleCredentials.fromStream(
                new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8))
        ).createScoped(
                Arrays.asList("https://www.googleapis.com/auth/cloud-platform")
        );
        System.out.println(credentials.getAuthenticationType());

        return StorageOptions.newBuilder()
                .setCredentials(credentials)
                .build()
                .getService();
    }
}