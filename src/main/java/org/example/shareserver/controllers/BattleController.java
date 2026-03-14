package org.example.shareserver.controllers;

import org.example.shareserver.components.AuthHeaderHelper;
import org.example.shareserver.services.BattleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("battles")
public class BattleController {
    @Autowired
    private BattleService battleService;
    @Autowired
    private AuthHeaderHelper authHeaderHelper;

    @PostMapping("generate-battle")
    public ResponseEntity<?> generate(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        Optional<String> userId = authHeaderHelper.getUserIdFromAuthHeader(authHeader);
        if (userId.isEmpty()) {
            return ResponseEntity.status(401).body("Unauthorized");
        }
        return battleService.generateBattle(userId.get());
    }

    @GetMapping("end-battle")
    public ResponseEntity<?> end(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        Optional<String> userId = authHeaderHelper.getUserIdFromAuthHeader(authHeader);
        if (userId.isEmpty()) {
            return ResponseEntity.status(401).body("Unauthorized");
        }
        return battleService.end(userId.get());
    }
}
