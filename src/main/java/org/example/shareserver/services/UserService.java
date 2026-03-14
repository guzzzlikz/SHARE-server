package org.example.shareserver.services;

import lombok.extern.slf4j.Slf4j;
import org.example.shareserver.components.HashComponent;
import org.example.shareserver.models.dtos.UserDTO;
import org.example.shareserver.models.entities.Item;
import org.example.shareserver.models.entities.User;
import org.example.shareserver.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private HashComponent hashComponent;

    @Autowired
    private JWTService jwtService;

    public ResponseEntity<?> changeProfileInfo(UserDTO userDto) {
        Optional<User> existingOpt = userRepository.findById(userDto.getId());
        if (existingOpt.isEmpty()) {
            return ResponseEntity.status(404).body("User not found");
        }
        User user = existingOpt.get();
        user.setNickname(userDto.getNickname() != null ? userDto.getNickname() : user.getNickname());
        user.setEmail(userDto.getEmail() != null ? userDto.getEmail().trim() : user.getEmail());
        user.setPathToPhoto(userDto.getPathToPhoto());
        user.setHp(userDto.getHp());
        user.setDamage(userDto.getDamage());
        user.setCoins(userDto.getCoins());
        user.setGems(userDto.getGems());
        user.setItems(userDto.getItems());
        if (userDto.getPassword() != null && !userDto.getPassword().isBlank()) {
            user.setPassword(hashComponent.hash(userDto.getPassword()));
        }
        userRepository.save(user);
        return ResponseEntity.ok(user);
    }

    public ResponseEntity<?> changeItems(String authHeader, List<Item> items) {
        if (authHeader == null || authHeader.isEmpty() || !authHeader.startsWith("Bearer ")) {
            log.warn("Possible XSS attack!");
            return ResponseEntity.status(403).body("Token not found");
        }
        String token = authHeader.replace("Bearer ", "");
        String userId = jwtService.getDataFromToken(token);

        Optional<User> userOp = userRepository.findById(userId);

        if(userOp.isEmpty()) {
            return ResponseEntity.status(404).body("User not found");
        }

        User user = equipItems(userOp.get());
        userRepository.save(user);

        return ResponseEntity.ok(user);
    }

    private User equipItems(User user) {
        List<Item> items = user.getItems();
        if (items == null) {
            return user;
        }
        int extraDamage = 0;
        int extraHp = 0;
        for (Item item : items) {
            if (item != null && item.isEquipped()) {
                extraDamage += item.getDamage();
                extraHp += item.getHp();
            }
        }
        user.setDamage(user.getDamage() + extraDamage);
        user.setHp(user.getHp() + extraHp);
        return user;
    }
}
