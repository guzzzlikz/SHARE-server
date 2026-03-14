package org.example.shareserver.services;

import org.example.shareserver.models.entities.Item;
import org.example.shareserver.models.entities.User;
import org.example.shareserver.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class BattleService {
    @Autowired
    private JWTService jwtService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PhotoStorageService photoStorageService;

    public ResponseEntity<?> generateBattle(String token) {
        String id = jwtService.getDataFromToken(token.replace("Bearer ", ""));
        Optional<User> user = userRepository.findById(id);
        int hp = user.get().getHp();
        int atk = user.get().getDamage();
        for (Item i : user.get().getItems()) {
            hp += i.getHp();
            atk += i.getDamage();
        }
        user.get().setHp(hp);
        user.get().setDamage(atk);
        userRepository.save(user.get());
        return ResponseEntity.ok().body(user);
    }

    public ResponseEntity<?> end(String token) {
        String id = jwtService.getDataFromToken(token.replace("Bearer ", ""));
        Optional<User> user = userRepository.findById(id);
        int hp = user.get().getHp();
        int atk = user.get().getDamage();
        for (Item i : user.get().getItems()) {
            hp -= i.getHp();
            atk -= i.getDamage();
        }
        if (hp <= 0) {
            hp = 10;
        }
        if (atk <= 0) {
            atk = 10;
        }
        user.get().setHp(hp);
        user.get().setDamage(atk);
        userRepository.save(user.get());
        photoStorageService.removeBattlePhoto(user.get().getId());
        return ResponseEntity.ok().body(user);
    }
}
