package org.example.shareserver.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class MapsService {
    @Value("${gmaps.key}")
    private String mapKey;
    @Autowired
    private AiService aiService;

    public ResponseEntity<?> getStreetFromCoordinates(double latitude, double longitude, String lang) {
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
            ResponseEntity<?> storyResp = aiService.generateStory(address, lang);
            String story = storyResp.getBody() != null ? storyResp.getBody().toString() : "";
            return ResponseEntity.ok().body(story);
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

    public ResponseEntity<?> getCityFromCoordinates(double latitude, double longitude) {
        WebClient webClient = WebClient.create();
        String response = webClient
                .get()
                .uri("https://maps.googleapis.com/maps/api/geocode/json?latlng="
                        + latitude + "," + longitude + "&key=" + mapKey)
                .retrieve()
                .bodyToMono(String.class)
                .block();
        ObjectMapper mapper = new ObjectMapper();
        String city = null;
        JsonNode root = null;
        try {
            root = mapper.readTree(response);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        JsonNode results = root.path("results");
        for (JsonNode result : results) {
            JsonNode components = result.path("address_components");
            for (JsonNode comp : components) {
                JsonNode types = comp.path("types");
                for (JsonNode type : types) {
                    if ("locality".equals(type.asText())) {
                        city = comp.path("long_name").asText();
                        break;
                    }
                }
                if (city != null) break;
            }
            if (city != null) break;
        }
        return ResponseEntity.status(200).body(city);
    }
}
