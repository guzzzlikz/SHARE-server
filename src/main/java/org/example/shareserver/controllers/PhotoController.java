package org.example.shareserver.controllers;

import org.example.shareserver.models.entities.User;
import org.example.shareserver.repositories.UserRepository;
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

    @PostMapping("/{userId}/uploadUser")
    public ResponseEntity<?> uploadUserProfilePhoto(@PathVariable String userId,
                                                    @RequestParam("file") MultipartFile file) {
        try {
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
    }

    @GetMapping("/{userId}")
    public ResponseEntity<?> getUserPhoto(@PathVariable String userId) {
        Optional<User> user = userRepository.findById(userId);
        if (user.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        String url = photoStorageService.getSignedUrl(user.get().getPathToPhoto());

        return ResponseEntity.ok(url);
    }
}
