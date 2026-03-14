package org.example.shareserver.services;

import org.example.shareserver.models.dtos.BattleStateDTO;
import org.example.shareserver.models.entities.BattleState;
import org.example.shareserver.models.entities.Enemy;
import org.example.shareserver.models.entities.Item;
import org.example.shareserver.models.entities.User;
import org.example.shareserver.repositories.BattleStateRepository;
import org.example.shareserver.repositories.EnemyRepository;
import org.example.shareserver.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
public class BattleService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private EnemyRepository enemyRepository;
    @Autowired
    private BattleStateRepository battleStateRepository;
    @Autowired
    private PhotoStorageService photoStorageService;

    private final Random random = new Random();

    public ResponseEntity<?> generateBattle(String userId, String enemyId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }
        Optional<Enemy> enemyOpt = enemyRepository.findById(enemyId);
        if (enemyOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Enemy not found");
        }

        User user = userOpt.get();
        Enemy enemy = enemyOpt.get();

        // Compute effective stats from items — do NOT persist changes to User
        List<Item> items = user.getItems() != null ? user.getItems() : Collections.emptyList();
        int effectiveHp = user.getHp();
        for (Item item : items) {
            if (item.isEquipped()) {
                effectiveHp += item.getHp();
            }
        }

        // Close any previous unfinished battle for this user
        battleStateRepository.findByUserIdAndIsOverFalse(userId).ifPresent(old -> {
            old.setOver(true);
            battleStateRepository.save(old);
        });

        BattleState state = BattleState.builder()
                .userId(userId)
                .enemyId(enemyId)
                .playerHp(effectiveHp)
                .enemyHp(enemy.getHp())
                .isOver(false)
                .win(false)
                .log(new ArrayList<>(List.of("Battle started! You face " + enemy.getName() + ".")))
                .build();

        BattleState saved = battleStateRepository.save(state);
        return ResponseEntity.ok(battleStateToDTO(saved));
    }

    public ResponseEntity<?> attack(String userId, String enemyId) {
        Optional<BattleState> stateOpt = battleStateRepository.findByUserIdAndIsOverFalse(userId);
        if (stateOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No active battle found");
        }

        BattleState state = stateOpt.get();
        if (!state.getEnemyId().equals(enemyId)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Enemy mismatch");
        }

        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }
        Optional<Enemy> enemyOpt = enemyRepository.findById(enemyId);
        if (enemyOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Enemy not found");
        }

        User user = userOpt.get();
        Enemy enemy = enemyOpt.get();

        // Compute effective player damage from equipped items
        List<Item> items = user.getItems() != null ? user.getItems() : Collections.emptyList();
        int effectiveDmg = user.getDamage();
        for (Item item : items) {
            if (item.isEquipped()) {
                effectiveDmg += item.getDamage();
            }
        }

        // Player attacks enemy
        int playerDmg = Math.max(1, effectiveDmg + random.nextInt(5) - 2);
        int newEnemyHp = Math.max(0, state.getEnemyHp() - playerDmg);
        state.getLog().add("You deal " + playerDmg + " damage to " + enemy.getName() + "!");

        // Enemy retaliates
        int enemyDmg = Math.max(1, enemy.getDamageToEnemy() + random.nextInt(3) - 1);
        int newPlayerHp = Math.max(0, state.getPlayerHp() - enemyDmg);
        state.getLog().add(enemy.getName() + " deals " + enemyDmg + " damage to you!");

        state.setEnemyHp(newEnemyHp);
        state.setPlayerHp(newPlayerHp);

        if (newEnemyHp <= 0) {
            state.setOver(true);
            state.setWin(true);
            state.getLog().add("Victory! " + enemy.getName() + " has been defeated!");
        } else if (newPlayerHp <= 0) {
            state.setOver(true);
            state.setWin(false);
            state.getLog().add("You have been defeated...");
        }

        battleStateRepository.save(state);
        return ResponseEntity.ok(battleStateToDTO(state));
    }

    public ResponseEntity<?> end(String userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }
        User user = userOpt.get();

        // Clean up any active battle state
        battleStateRepository.findByUserIdAndIsOverFalse(userId).ifPresent(state -> {
            state.setOver(true);
            battleStateRepository.save(state);
        });

        photoStorageService.removeBattlePhoto(userId);
        return ResponseEntity.ok(user);
    }

    private BattleStateDTO battleStateToDTO(BattleState state) {
        return BattleStateDTO.builder()
                .battleId(state.getId())
                .playerHp(state.getPlayerHp())
                .enemyHp(state.getEnemyHp())
                .battleLog(state.getLog())
                .isOver(state.isOver())
                .win(state.isWin())
                .build();
    }
}
