package org.example.shareserver.controllers;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.shareserver.components.AuthHeaderHelper;
import org.example.shareserver.services.AiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

@RestController
@RequestMapping("/ai")
public class AiController {
    @Autowired
    private AiService aiService;
    @Autowired
    private AuthHeaderHelper authHeaderHelper;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    private static class AiDTO {
        @jakarta.validation.constraints.NotBlank(message = "Message is required")
        private String message;
    }

    @PostMapping("/ask")
    public ResponseEntity<?> ask(@jakarta.validation.Valid @RequestBody AiDTO dto) {
        return aiService.ask(dto.getMessage());
    }

    @PostMapping("/generate-profile")
    public ResponseEntity<?> generateProfile(@RequestPart("file") MultipartFile file,
                                              @RequestHeader(value = "Authorization", required = false) String authHeader) {
        Optional<String> userId = authHeaderHelper.getUserIdFromAuthHeader(authHeader);
        if (userId.isEmpty()) {
            return ResponseEntity.status(401).body("Unauthorized");
        }
        return aiService.generateProfile(file, userId.get());
    }

    @PostMapping("/check")
    public ResponseEntity<?> check(@RequestPart("file") MultipartFile file,
                                    @RequestParam("lat") double lat,
                                    @RequestParam("lng") double lng) {
        return aiService.check(file, lat, lng);
    }

    @PostMapping("/generate-battle-photo")
    public ResponseEntity<?> generateBattlePhoto(@RequestPart("file") MultipartFile file,
                                                 @RequestHeader(value = "Authorization", required = false) String authHeader,
                                                 @RequestParam(required = false) String mobId) {
        Optional<String> userId = authHeaderHelper.getUserIdFromAuthHeader(authHeader);
        if (userId.isEmpty()) {
            return ResponseEntity.status(401).body("Unauthorized");
        }
        return aiService.generateBattlePhoto(file, userId.get());
    }

    @GetMapping("/generate-chests")
    public ResponseEntity<?> generateChests(@RequestParam(value = "city", required = false) String city) {
        return aiService.generateChests(city);
    }

    @GetMapping("/generate-enemies")
    public ResponseEntity<?> generateEnemies(@RequestParam(value = "city", required = false) String city,
                                             @RequestParam(value = "place", required = false) String place){
        return aiService.generateEnemies(city, place);
    }
}
