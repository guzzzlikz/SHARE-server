package org.example.shareserver.services;

import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import lombok.extern.slf4j.Slf4j;
import org.example.shareserver.models.entities.User;
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

    @Value("${gcs.bucket.photo.name}")
    private String bucketName;

    public String uploadUserProfilePhoto(MultipartFile file, String userId) throws IOException {
        log.info("Upload user profile method has been called");
        String blobName = "photos/user/" + userId + "/" + file.getOriginalFilename();
        BlobInfo blobInfo = BlobInfo.newBuilder(bucketName, blobName)
                .setContentType(file.getContentType())
                .build();
        storage.create(blobInfo, file.getBytes());
        User user = userRepository.findById(userId).orElseThrow();
        user.setPathToPhoto(blobName);
        return blobName;
    }

    public String getSignedUrl(String objectName) {
        BlobInfo blobInfo = BlobInfo.newBuilder(bucketName, objectName).build();
        URL signedUrl = storage.signUrl(
                blobInfo,
                120, TimeUnit.HOURS,
                Storage.SignUrlOption.withV4Signature()
        );
        return signedUrl.toString();
    }
}
