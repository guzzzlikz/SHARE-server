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

    @Autowired
    private JWTService jwtService;

    public ResponseEntity<?> killEnemyById(String enemyId, String authToken){
        Optional<Enemy> enemyOpt = enemyRepository.findById(enemyId);

        if (enemyOpt.isEmpty()) {
            return ResponseEntity.status(404).body("Enemy not found");
        }

        String token = authToken.replace("Bearer ", "");
        String userId = jwtService.getDataFromToken(token);

        Optional<User> userOpt = userRepository.findById(userId);

        if (userOpt.isEmpty()) {
            return ResponseEntity.status(404).body("User not found");
        }

        Enemy enemy = enemyOpt.get();
        User user = userOpt.get();

        Random random = new Random();
        int coinPrize = (int) Math.round( 200 * random.nextDouble());
        int gemsPrize = (int) Math.round( 20 * random.nextDouble());

        user.setCoins(coinPrize);
        user.setGems(gemsPrize);

        enemyRepository.save(enemy);
        userRepository.save(user);
        return ResponseEntity.ok("Enemy was disable successfully, time expiration: " + enemy.getKilledAt());
    }
}
