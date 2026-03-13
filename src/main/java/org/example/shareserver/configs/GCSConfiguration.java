package org.example.shareserver.configs;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import org.springframework.context.annotation.Configuration;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Configuration
public class GCSConfiguration {
    public GCSConfiguration() throws IOException {
        String json = System.getProperty("gcs.key");

        GoogleCredentials credentials = GoogleCredentials.fromStream
                (new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8)));

        Storage storage = StorageOptions.newBuilder()
                .setCredentials(credentials)
                .build()
                .getService();
    }
}
