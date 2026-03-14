package org.example.shareserver.services;

import lombok.extern.slf4j.Slf4j;
import org.example.shareserver.components.HashComponent;
import org.example.shareserver.models.ApiResponse;
import org.example.shareserver.models.entities.User;
import org.example.shareserver.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@EnableCaching
public class AuthService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private HashComponent hashComponent;
    @Autowired
    private JWTService jwtService;

    @Cacheable("register")
    public ResponseEntity<?> register(User user) {
        StringBuilder errorMsg = new StringBuilder();
        if (userRepository.findByEmail(user.getEmail()) != null) {
            errorMsg.append("User is already registered\n");
            log.info("AuthService has been called but user is already registered");
            return ResponseEntity.badRequest().body(errorMsg.toString());
        }
        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            errorMsg.append("Password is required\n");
            log.info("AuthService has been called but password is empty");
        }
        else {user.setPassword(hashComponent.hash(user.getPassword()));}
        if (user.getEmail() == null || user.getEmail().isEmpty()) {
            errorMsg.append("Email is required\n");
            log.info("AuthService has been called but email is empty");
        }
        else {user.setEmail(user.getEmail().trim());}

        if (user.getNickname() == null || user.getNickname().isEmpty()) {
            errorMsg.append("Nickname is required\n");
            log.info("AuthService has been called but nickname is empty");
        }
        else {user.setNickname(user.getNickname().trim());}
        userRepository.save(user);
        String token = jwtService.generateToken(user.getId());
        return ResponseEntity.status(200).body(new ApiResponse(token, "User registered successfully", user));
    }

    @Cacheable("login")
    public ResponseEntity<?> login(String email, String password) {
        StringBuilder errorMsg = new StringBuilder();
        if (email == null || email.isEmpty()) {
            errorMsg.append("Email is required\n");
            log.info("AuthService has been called but email is empty");
        }
        if (password == null || password.isEmpty()) {
            errorMsg.append("Password is required\n");
            log.info("AuthService has been called but password is empty");
        }
        if (!errorMsg.toString().isEmpty()) {
            return ResponseEntity.badRequest().body(errorMsg.toString());
        }
        User mongoUser = userRepository.findByEmail(email);
        if (mongoUser == null) {
            log.info("AuthService has been called but there is no user");
            return ResponseEntity.badRequest().body("User not found");
        }
        if (!mongoUser.getPassword().equals(hashComponent.hash(password))) {
            log.info("AuthService has been called but password is incorrect");
            return ResponseEntity.badRequest().body("Incorrect password");
        }

        String token = jwtService.generateToken(mongoUser.getId());
        mongoUser.setPassword(null);
        return ResponseEntity.status(200).body(new ApiResponse(token, "User logged in successfully", mongoUser));
    }
}
