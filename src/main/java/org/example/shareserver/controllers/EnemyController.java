package org.example.shareserver.controllers;

import org.example.shareserver.models.entities.Enemy;
import org.example.shareserver.models.entities.User;
import org.example.shareserver.repositories.EnemyRepository;
import org.example.shareserver.repositories.UserRepository;
import org.example.shareserver.services.EnemyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/enemy")
public class EnemyController {

    @Autowired
    private EnemyService enemyService;

    @Autowired
    private EnemyRepository enemyRepository;

    @GetMapping("/{city}")
    public ResponseEntity<?> findByCity(@PathVariable String city) {
        List<Enemy> enemies = enemyRepository.findByCity(city);
        return ResponseEntity.ok().body(enemies);
    }

    @PostMapping("/{enemyId}/kill")
    public ResponseEntity<?> killEnemyById(@PathVariable String enemyId, @PathVariable String userId,
                                           @RequestHeader("Authorization") String authHeader){

        return enemyService.killEnemyById(enemyId, authHeader);
    }

    @PostMapping("/create")
    public ResponseEntity<?> createEnemy(@RequestBody Enemy enemy) {
        enemyRepository.save(enemy);
        return ResponseEntity.ok("Created");
    }
}
