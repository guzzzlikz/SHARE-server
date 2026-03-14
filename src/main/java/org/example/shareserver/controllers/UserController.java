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
    public ResponseEntity<?> changeProfileInfo(@RequestBody UserDTO userDTO) {
        return userService.changeProfileInfo(userDTO);
    }

    @PostMapping("/items")
    public ResponseEntity<?> changeProfileInfo(@RequestBody List<Item> items,@RequestHeader("Authorization") String authHeader) {
        return userService.changeItems(authHeader, items);
    }
}
