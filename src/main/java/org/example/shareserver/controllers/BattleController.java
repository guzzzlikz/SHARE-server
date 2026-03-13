package org.example.shareserver.controllers;

import org.example.shareserver.models.dtos.BattleDTO;
import org.example.shareserver.repositories.BattleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("battles")
public class BattleController {
    @Autowired
    private BattleRepository battleRepository;
    @PostMapping("generate")
    public ResponseEntity<?> generate() {
        BattleDTO battleDTO = BattleDTO.builder()
                .id("111")
                .mobId("121")
                .build();
        battleRepository.save(battleDTO);
        return ResponseEntity.ok().build();
    }
}
