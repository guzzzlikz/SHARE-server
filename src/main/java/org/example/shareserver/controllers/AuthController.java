package org.example.shareserver.controllers;

import org.example.shareserver.models.User;
import org.example.shareserver.services.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("auth")
public class AuthController {
    @Autowired
    private AuthService authService;
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        return authService.register(user);
    }
    @GetMapping("/login")
    public ResponseEntity<?> login(@RequestParam("login") String email, @RequestParam("password") String password) {
        return authService.login(email, password);
    }
}
