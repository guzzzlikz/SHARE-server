package org.example.shareserver.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Service
public class MapsService {
    @Value("${gmaps.key}")
    private String mapKey;
    @Autowired
    private AiService aiService;

    public ResponseEntity<?> getStreetFromCoordinates(double latitude, double longitude) {
        WebClient webClient = WebClient.create();
        String response = webClient
                .get()
                .uri("https://maps.googleapis.com/maps/api/geocode/json?latlng="
                        + latitude + "," + longitude + "&key=" + mapKey)
                .retrieve()
                .bodyToMono(String.class)
                .block();
        ObjectMapper mapper = new ObjectMapper();
        try {
            JsonNode root = mapper.readTree(response);
            JsonNode results = root.path("results");
            if (results.isEmpty() || !results.isArray()) {
                return ResponseEntity.status(404).body("No address found for coordinates");
            }
            String address = results.get(0).path("formatted_address").asText();
            return ResponseEntity.ok().body(aiService.generateStory(address));
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Failed to parse geocode response");
        }
    }

    public ResponseEntity<?> getQuizOnStreetFromCoordinates(double latitude, double longitude) {
        WebClient webClient = WebClient.create();
        String response = webClient
                .get()
                .uri("https://maps.googleapis.com/maps/api/geocode/json?latlng="
                        + latitude + "," + longitude + "&key=" + mapKey)
                .retrieve()
                .bodyToMono(String.class)
                .block();
        ObjectMapper mapper = new ObjectMapper();
        try {
            JsonNode root = mapper.readTree(response);
            JsonNode results = root.path("results");
            if (results.isEmpty() || !results.isArray()) {
                return ResponseEntity.status(404).body("No address found for coordinates");
            }
            String address = results.get(0).path("formatted_address").asText();
            return aiService.generateQuiz(address);
        }catch (Exception e) {
            return ResponseEntity.status(500).body("Failed to parse geocode response");
        }
    }
}
