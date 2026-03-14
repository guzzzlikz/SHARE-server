package org.example.shareserver.services;

import org.example.shareserver.models.entities.Enemy;
import org.example.shareserver.models.entities.User;
import org.example.shareserver.repositories.EnemyRepository;
import org.example.shareserver.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.Optional;

@Service
public class EnemyService {

    @Autowired
    private EnemyRepository enemyRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AiService aiService;

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
        if (enemy.isBoss()) {
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

    public ResponseEntity<?> verifyLocation(String enemyId, MultipartFile photo, double lat, double lng) {
        Optional<Enemy> enemyOpt = enemyRepository.findById(enemyId);
        if (enemyOpt.isEmpty()) {
            return ResponseEntity.status(404).body("Enemy not found");
        }

        Enemy enemy = enemyOpt.get();
        double distanceMeters = haversineMeters(lat, lng, enemy.getLatitude(), enemy.getLongitude());

        if (distanceMeters > 50) {
            return ResponseEntity.ok(Map.of(
                    "canInteract", false,
                    "message", "Too far away (~" + Math.round(distanceMeters) + "m). Must be within 50m."
            ));
        }

        ResponseEntity<?> aiResponse = aiService.check(photo, lat, lng);
        if (aiResponse.getStatusCode().is2xxSuccessful()) {
            return ResponseEntity.ok(Map.of(
                    "canInteract", true,
                    "message", "Location verified!"
            ));
        } else {
            return ResponseEntity.ok(Map.of(
                    "canInteract", false,
                    "message", "Photo does not match the location."
            ));
        }
    }

    private static double haversineMeters(double lat1, double lng1, double lat2, double lng2) {
        final double R = 6_371_000; // Earth radius in meters
        double phi1 = Math.toRadians(lat1);
        double phi2 = Math.toRadians(lat2);
        double dPhi = Math.toRadians(lat2 - lat1);
        double dLambda = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dPhi / 2) * Math.sin(dPhi / 2)
                + Math.cos(phi1) * Math.cos(phi2)
                * Math.sin(dLambda / 2) * Math.sin(dLambda / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }
}
