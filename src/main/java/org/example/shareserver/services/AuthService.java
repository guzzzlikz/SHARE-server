package org.example.shareserver.services;

import lombok.extern.slf4j.Slf4j;
import org.example.shareserver.components.HashComponent;
import org.example.shareserver.models.ApiResponse;
import org.example.shareserver.models.User;
import org.example.shareserver.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class AuthService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private HashComponent hashComponent;

    public ResponseEntity<?> register(User user) {
        StringBuilder errorMsg = new StringBuilder();
        if (user.getId() == null || user.getId().isEmpty()) {
            errorMsg.append("User id is required\n");
            log.info("AuthService has been called but id is empty");
        }
        else {user.setId(user.getId().trim());}
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
        if (user.getFirstName() == null || user.getFirstName().isEmpty()) {
            errorMsg.append("First name is required\n");
            log.info("AuthService has been called but first name is empty");
        }
        else {user.setFirstName(user.getFirstName().trim());}
        if (user.getLastName() == null || user.getLastName().isEmpty()) {
            errorMsg.append("Last name is required\n");
            log.info("AuthService has been called but last name is empty");
        }
        else {user.setLastName(user.getLastName().trim());}
        if (!errorMsg.isEmpty()) {
            return ResponseEntity.badRequest().body(errorMsg.toString());
        }
        userRepository.save(user);
        user.setPassword(null);
        return ResponseEntity.status(200).body(new ApiResponse("User registered successfully", user));
    }

    public ResponseEntity<?> login(String email, String password) {
        StringBuilder errorMsg = new StringBuilder();
        if (email == null || email.isEmpty()) {
            errorMsg.append("Email is required\n");
        }
        if (password == null || password.isEmpty()) {
            errorMsg.append("Password is required\n");
        }
        if (!errorMsg.toString().isEmpty()) {
            return ResponseEntity.badRequest().body(errorMsg.toString());
        }
        User mongoUser = userRepository.findByEmail(email);
        if (mongoUser == null) {
            return ResponseEntity.badRequest().body("User not found");
        }
        if (!mongoUser.getPassword().equals(hashComponent.hash(mongoUser.getPassword()))) {
            return ResponseEntity.badRequest().body("Incorrect password");
        }
        mongoUser.setPassword(null);
        return ResponseEntity.status(200).body(new ApiResponse("User logged in successfully", mongoUser));
    }
}
