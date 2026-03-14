package org.example.shareserver.services;

import org.example.shareserver.models.entities.Enemy;
import org.example.shareserver.models.entities.Item;
import org.example.shareserver.models.entities.User;
import org.example.shareserver.repositories.EnemyRepository;
import org.example.shareserver.repositories.ItemRepository;
import org.example.shareserver.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
public class EnemyService {

    @Autowired
    private EnemyRepository enemyRepository;

    @Autowired
    private UserRepository userRepository;

    public ResponseEntity<?> killEnemyById(String enemyId, String userId) {
        Optional<Enemy> enemyOpt = enemyRepository.findById(enemyId);

        if (enemyOpt.isEmpty()) {
            return ResponseEntity.status(404).body("Enemy not found");
        }

        Optional<User> userOpt = userRepository.findById(userId);

        if (userOpt.isEmpty()) {
            return ResponseEntity.status(404).body("User not found");
        }

        Enemy enemy = enemyOpt.get();
        User user = userOpt.get();

        int coinPrize;
        int gemsPrize = 0;
        int xp;
        if(enemy.isBoss()){
            coinPrize = 50;
            gemsPrize = 5;
            xp = 100;
        } else {
            coinPrize = 15;
            xp = 25;
        }


        user.setCoins(user.getCoins() + coinPrize);
        user.setGems(user.getGems() + gemsPrize);
        user.setXp(user.getXp() + xp);


        enemyRepository.save(enemy);
        userRepository.save(user);
        return ResponseEntity.ok("Enemy was killed successfully");
    }
}
