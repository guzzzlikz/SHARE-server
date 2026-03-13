package org.example.shareserver.controllers;

import org.example.shareserver.models.entities.Enemy;
import org.example.shareserver.models.entities.Item;
import org.example.shareserver.models.entities.User;
import org.example.shareserver.repositories.EnemyRepository;
import org.example.shareserver.repositories.ItemRepository;
import org.example.shareserver.repositories.UserRepository;
import org.example.shareserver.services.BucketType;
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
    private EnemyRepository enemyRepository;

    @Autowired
    private ItemRepository itemRepository;

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

    @PostMapping("/{enemyId}/uploadEnemy")
    public ResponseEntity<?> uploadItemProfilePhoto(@PathVariable String enemyId,
                                                    @RequestParam("file") MultipartFile file) {
        try {
            String gcsPath = photoStorageService.uploadItemProfilePhoto(file, enemyId);
            Optional<Item> user = itemRepository.findById(enemyId);
            if (user.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            user.get().setPathToPhoto(gcsPath);
            itemRepository.save(user.get());
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Upload failed: " + e.getMessage());
        }
    }

    @PostMapping("/{itemId}/uploadItem")
    public ResponseEntity<?> uploadEnemyProfilePhoto(@PathVariable String itemId,
                                                     @RequestParam("file") MultipartFile file) {
        try {
            String gcsPath = photoStorageService.uploadEnemyProfilePhoto(file, itemId);
            Optional<Enemy> user = enemyRepository.findById(itemId);
            if (user.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            user.get().setPathToPhoto(gcsPath);
            enemyRepository.save(user.get());
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

        String url = photoStorageService.getSignedUrl(user.get().getPathToPhoto(), BucketType.USER);

        return ResponseEntity.ok(url);
    }
}
