package org.example.shareserver.controllers;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.shareserver.services.AiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/ai")
public class AiController {
    @Autowired
    private AiService aiService;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    private static class AiDTO {
        private String message;
    }

    @PostMapping("/ask")
    public ResponseEntity<?> ask(@RequestBody AiDTO dto) {
        return aiService.ask(dto.message);
    }
    @PostMapping("/generate-profile")
    public ResponseEntity<?> generateProfile(@RequestPart("file") MultipartFile file,
                                            @RequestHeader("Authorization") String token) {
        return aiService.generateProfile(file, token);
    }
}
