package org.example.shareserver.services;

import org.example.shareserver.models.dtos.OpenAIImageDTO;
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

        JsonNode root = mapper.readTree(response);

        String address = root
                .path("results")
                .get(0)
                .path("formatted_address")
                .asText();

        System.out.println(address);
        return ResponseEntity.ok().body(address);
    }
}
