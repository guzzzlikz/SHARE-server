package org.example.shareserver.controllers;
import org.example.shareserver.models.entities.Item;
import org.example.shareserver.models.entities.User;
import org.example.shareserver.repositories.ItemRepository;
import org.example.shareserver.repositories.UserRepository;
import org.example.shareserver.services.JWTService;
import org.example.shareserver.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/item")
public class ItemController {

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private JWTService jwtService;

    @PostMapping("/create")
    public ResponseEntity<?> createEnemy(@RequestBody Item item) {
        itemRepository.save(item);
        return ResponseEntity.ok("Created");
    }

    @GetMapping
    public ResponseEntity<?> getItem(@RequestHeader("Authorization") String authHeader){
        String userId = jwtService.getDataFromToken(authHeader);
        Optional<User> user = userRepository.findById(userId);
        if (user.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        // TODO add ai item choosing
        Item item = new Item();
        user.get().getItems().add(item);
        userService.changeItems(authHeader, user.get().getItems());

        return ResponseEntity.ok(item);
    }
}
