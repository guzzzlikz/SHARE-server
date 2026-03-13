package org.example.shareserver.services;

import org.example.shareserver.mappers.UserMapper;
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
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserMapper userMapper;

    public ResponseEntity<?> changeProfileInfo(UserDTO userDto) {
        User user = userMapper.toUser(userDto);
        if(userRepository.findById(user.getId()).isEmpty()) {
            return ResponseEntity.status(404).body("User not found");
        }

        userRepository.save(user);
        return ResponseEntity.ok(user);
    }

    public ResponseEntity<?> changeItems(String userId, List<Item> items) {
        Optional<User> userOp = userRepository.findById(userId);

        if(userOp.isEmpty()) {
            return ResponseEntity.status(404).body("User not found");
        }

        User user = equipItems(userOp.get());
        userRepository.save(user);

        return ResponseEntity.ok(user);
    }

    private User equipItems(User user) {
        user.getItems().stream().map(item -> {
            if (item.isEquipped()) {
                user.setDamage(user.getDamage() + item.getDamage());
                user.setHp(user.getHp() + item.getHp());
            }
            return item;
        });
        return user;
    }
}
