package org.example.shareserver.controllers;

import jakarta.validation.Valid;
import org.example.shareserver.components.AuthHeaderHelper;
import org.example.shareserver.models.dtos.CreateEnemyDTO;
import org.example.shareserver.models.entities.Enemy;
import org.example.shareserver.repositories.EnemyRepository;
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

    @Autowired
    private AuthHeaderHelper authHeaderHelper;

    @GetMapping("/{city}")
    public ResponseEntity<?> findByCity(@PathVariable String city) {
        List<Enemy> enemies = enemyRepository.findByCity(city);
        return ResponseEntity.ok().body(enemies);
    }

    @PostMapping("/{enemyId}/kill")
    public ResponseEntity<?> killEnemyById(@PathVariable String enemyId,
                                           @RequestHeader(value = "Authorization", required = false) String authHeader) {
        Optional<String> userId = authHeaderHelper.getUserIdFromAuthHeader(authHeader);
        if (userId.isEmpty()) {
            return ResponseEntity.status(401).body("Unauthorized");
        }
        return enemyService.killEnemyById(enemyId, userId.get());
    }

    @PostMapping("/create")
    public ResponseEntity<?> createEnemy(@Valid @RequestBody CreateEnemyDTO dto) {
        Enemy enemy = new Enemy();
        enemy.setName(dto.getName());
        enemy.setPathToPhoto(dto.getPathToPhoto());
        enemy.setCity(dto.getCity());
        enemy.setLongitude(dto.getLongitude());
        enemy.setLatitude(dto.getLatitude());
        enemy.setHp(dto.getHp());
        enemy.setDamageToEnemy(dto.getDamageToEnemy());
        enemy.setBoss(dto.isBoss());
        enemy.setChestType(dto.getChestType());
        Enemy saved = enemyRepository.save(enemy);
        return ResponseEntity.ok(saved);
    }
}
