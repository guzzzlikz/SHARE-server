package org.example.shareserver.controllers;

import org.example.shareserver.models.dtos.LoginDTO;
import org.example.shareserver.models.dtos.UserDTO;
import org.example.shareserver.models.entities.Item;
import org.example.shareserver.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user/info")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/profile")
    public ResponseEntity<?> changeProfileInfo(@RequestBody LoginDTO userDTO) {
        return userService.changeProfileInfo(userDTO);
    }

    @PostMapping("/items/{userId}")
    public ResponseEntity<?> changeProfileInfo(@PathVariable String userId, @RequestBody List<Item> items) {
        return userService.changeItems(userId, items);
    }
}
