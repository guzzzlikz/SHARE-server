package org.example.shareserver.services;

import org.example.shareserver.models.entities.Item;
import org.example.shareserver.models.entities.User;
import org.example.shareserver.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class BattleService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PhotoStorageService photoStorageService;

    public ResponseEntity<?> generateBattle(String userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }
        User user = userOpt.get();
        List<Item> items = user.getItems() != null ? user.getItems() : Collections.emptyList();
        int hp = user.getHp();
        int atk = user.getDamage();
        for (Item i : items) {
            hp += i.getHp();
            atk += i.getDamage();
        }
        user.setHp(hp);
        user.setDamage(atk);
        userRepository.save(user);
        return ResponseEntity.ok().body(user);
    }

    public ResponseEntity<?> end(String userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }
        User user = userOpt.get();
        List<Item> items = user.getItems() != null ? user.getItems() : Collections.emptyList();
        int hp = user.getHp();
        int atk = user.getDamage();
        for (Item i : items) {
            hp -= i.getHp();
            atk -= i.getDamage();
        }
        if (hp <= 0) {
            hp = 10;
        }
        if (atk <= 0) {
            atk = 10;
        }
        user.setHp(hp);
        user.setDamage(atk);
        userRepository.save(user);
        photoStorageService.removeBattlePhoto(user.getId());
        return ResponseEntity.ok().body(user);
    }
}
