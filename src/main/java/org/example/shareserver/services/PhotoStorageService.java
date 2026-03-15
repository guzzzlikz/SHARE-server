package org.example.shareserver.services;

import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import lombok.extern.slf4j.Slf4j;
import org.example.shareserver.models.dtos.BattleDTO;
import org.example.shareserver.models.entities.Enemy;
import org.example.shareserver.models.entities.User;
import org.example.shareserver.repositories.BattleRepository;
import org.example.shareserver.repositories.EnemyRepository;
import org.example.shareserver.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URL;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class PhotoStorageService {
    @Autowired
    private Storage storage;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EnemyRepository enemyRepository;

    @Value("${gcs.bucket.photo.name}")
    private String userBucketName;

    @Value("${gcs.bucket.battle.photo.name}")
    private String battleBucketName;

    @Value("${gcs.bucket.mob.photo.name}")
    private String mobBucketName;

    @Value("${gcs.bucket.item.photo.name}")
    private String itemBucketName;

    @Value("${gcs.bucket.gmaps.photo.name}")
    private String gmapsBucketName;

    @Autowired
    private BattleRepository battleRepository;

    @Value("${gcs.bucket.photo.name}")
    private String bucketName;

    public String uploadUserProfilePhoto(MultipartFile file, String userId) throws IOException {
        log.info("Upload user profile method has been called");
        String blobName = "photos/user/" + userId + "/" + file.getOriginalFilename();
        BlobInfo blobInfo = BlobInfo.newBuilder(userBucketName, blobName)
                .setContentType(file.getContentType())
                .build();
        storage.create(blobInfo, file.getBytes());
        User user = userRepository.findById(userId).orElseThrow();
        user.setPathToPhoto(blobName);
        userRepository.save(user);
        return blobName;
    }

    public String uploadEnemyProfilePhoto(MultipartFile file, String enemyId) throws IOException {
        String blobName = "photos/enemy/" + enemyId + "/" + file.getOriginalFilename();
        BlobInfo blobInfo = BlobInfo.newBuilder(mobBucketName, blobName)
                .setContentType(file.getContentType())
                .build();
        storage.create(blobInfo, file.getBytes());
        log.info("enemy id: {}", enemyId);
        Enemy enemy = enemyRepository.findById(enemyId).orElseThrow();
        enemy.setPathToPhoto(blobName);
        enemyRepository.save(enemy);
        return blobName;
    }

    public String uploadItemProfilePhoto(MultipartFile file, String itemId) throws IOException {
        String blobName = "photos/item/" + itemId + "/" + file.getOriginalFilename();
        BlobInfo blobInfo = BlobInfo.newBuilder(itemBucketName, blobName)
                .setContentType(file.getContentType())
                .build();
        storage.create(blobInfo, file.getBytes());

        return blobName;
    }

    public String getSignedUrl(String objectName, BucketType bucketType) {
        String bucketName = switch (bucketType){
            case USER ->  userBucketName;
            case MOB -> mobBucketName;
            case ITEM -> itemBucketName;
            case BATTLE -> battleBucketName;
            case GMAPS -> gmapsBucketName;
        };
        if (bucketName == null) {
            return "";
        }
        BlobInfo blobInfo = BlobInfo.newBuilder(bucketName, objectName).build();
        URL signedUrl = storage.signUrl(
                blobInfo,
                120, TimeUnit.HOURS,
                Storage.SignUrlOption.withV4Signature()
        );
        return signedUrl.toString();
    }

    public String uploadBattlePhoto(MultipartFile file, BattleDTO battleDTO) throws IOException {
        log.info("Upload battle photo has been called");
        String blobName = "photos/battle/" +
                battleDTO.getUserId() + "/"
                + "battle";
        BlobInfo blobInfo = BlobInfo.newBuilder(battleBucketName, blobName)
                .setContentType(file.getContentType())
                .build();
        storage.create(blobInfo, file.getBytes());
        BattleDTO battleDTOToDb = battleRepository.findById(battleDTO.getId()).orElseThrow();
        battleDTOToDb.setPathToPhoto(blobName);
        battleRepository.save(battleDTOToDb);
        return blobName;
    }
    public void removeBattlePhoto(String id) {
        String blobName = "photos/battle/" + id + "/battle";
        storage.delete(battleBucketName, blobName);
    }
}
