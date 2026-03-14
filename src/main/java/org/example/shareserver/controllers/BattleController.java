package org.example.shareserver.controllers;

import org.example.shareserver.services.BattleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("battles")
public class BattleController {
    @Autowired
    private BattleService battleService;
    @PostMapping("generate-battle")
    public ResponseEntity<?> generate(@RequestHeader("Authorization") String token) {
        return battleService.generateBattle(token);
    }
    @GetMapping("end-battle")
    public ResponseEntity<?> end(@RequestHeader("Authorization") String token) {
        return battleService.end(token);
    }
}
