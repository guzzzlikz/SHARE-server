package org.example.shareserver.controllers;

import lombok.extern.slf4j.Slf4j;
import org.example.shareserver.components.AuthHeaderHelper;
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
@Slf4j
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

    @Autowired
    private AuthHeaderHelper authHeaderHelper;

    @PostMapping("uploadUser")
    public ResponseEntity<?> uploadUserProfilePhoto(@RequestHeader(value = "Authorization", required = false) String authHeader,
                                                     @RequestParam("file") MultipartFile file) {
        Optional<String> userIdOpt = authHeaderHelper.getUserIdFromAuthHeader(authHeader);
        if (userIdOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }
        String userId = userIdOpt.get();
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

    @PostMapping("/{itemId}/uploadItem")
    public ResponseEntity<?> uploadItemProfilePhoto(@PathVariable String itemId,
                                                    @RequestParam("file") MultipartFile file) {
        try {
            String gcsPath = photoStorageService.uploadItemProfilePhoto(file, itemId);
            Optional<Item> user = itemRepository.findById(itemId);
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

    @PostMapping("/{enemyId}/uploadEnemy")
    public ResponseEntity<?> uploadEnemyProfilePhoto(@PathVariable String enemyId,
                                                     @RequestParam("file") MultipartFile file) {
        try {
            log.info("enemy id: {}", enemyId);
            String gcsPath = photoStorageService.  uploadEnemyProfilePhoto(file, enemyId);

            Optional<Enemy> user = enemyRepository.findById(enemyId);
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

    @GetMapping("/profile")
    public ResponseEntity<?> getUserPhoto(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        Optional<String> userIdOpt = authHeaderHelper.getUserIdFromAuthHeader(authHeader);
        if (userIdOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }
        Optional<User> user = userRepository.findById(userIdOpt.get());
        if (user.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        String pathToPhoto = user.get().getPathToPhoto();
        if (pathToPhoto == null || pathToPhoto.isBlank()) {
            return ResponseEntity.noContent().build();
        }
        String url = photoStorageService.getSignedUrl(pathToPhoto, BucketType.USER);
        return ResponseEntity.ok(url);
    }
    @GetMapping("/enemy/${mobId}")
    public ResponseEntity<?> getPhotoById(@PathVariable String mobId) {
        return ResponseEntity.status(200).body(enemyRepository.findById(mobId));
    }
}
