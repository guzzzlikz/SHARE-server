package org.example.shareserver.controllers;

import org.example.shareserver.models.entities.User;
import org.example.shareserver.repositories.UserRepository;
import org.example.shareserver.services.JWTService;
import org.example.shareserver.services.PhotoStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

@RestController
@RequestMapping("/photo")
public class PhotoController {

    @Autowired
    private PhotoStorageService photoStorageService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JWTService jwtService;

    @PostMapping("uploadUser")
    public ResponseEntity<?> uploadUserProfilePhoto(@RequestHeader("Authorization") String token, @RequestParam("file") MultipartFile file) {
        try {
            String userId = jwtService.getDataFromToken(token);
            String gcsPath = photoStorageService.uploadUserProfilePhoto(file, userId);
            Optional<User> user = userRepository.findById(userId);
            if (user.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            user.get().setPathToPhoto(gcsPath);
            userRepository.save(user.get());
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Upload failed: " + e.getMessage());
        }
    }}
