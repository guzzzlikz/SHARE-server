package org.example.shareserver.controllers;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.shareserver.services.AiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
