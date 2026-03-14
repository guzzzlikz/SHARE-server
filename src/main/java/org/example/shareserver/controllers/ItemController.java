package org.example.shareserver.controllers;

import jakarta.validation.Valid;
import org.example.shareserver.components.AuthHeaderHelper;
import org.example.shareserver.models.dtos.CreateItemDTO;
import org.example.shareserver.models.entities.Item;
import org.example.shareserver.models.entities.User;
import org.example.shareserver.repositories.ItemRepository;
import org.example.shareserver.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/item")
public class ItemController {

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthHeaderHelper authHeaderHelper;

    @PostMapping("/create")
    public ResponseEntity<?> createItem(@Valid @RequestBody CreateItemDTO dto) {
        Item item = new Item();
        item.setName(dto.getName());
        item.setPathToPhoto(dto.getPathToPhoto());
        item.setHp(dto.getHp());
        item.setDamage(dto.getDamage());
        item.setEquipped(dto.isEquipped());
        itemRepository.save(item);
        return ResponseEntity.ok(item);
    }

    @GetMapping
    public ResponseEntity<?> getItem(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        Optional<String> userIdOpt = authHeaderHelper.getUserIdFromAuthHeader(authHeader);
        if (userIdOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }
        String userId = userIdOpt.get();
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }
        User user = userOpt.get();
        List<Item> items = user.getItems();
        if (items == null) {
            items = new ArrayList<>();
            user.setItems(items);
        }
        Item item = new Item();
        items.add(item);
        userRepository.save(user);
        return ResponseEntity.ok(item);
    }
}
