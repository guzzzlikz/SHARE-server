package org.example.shareserver.services;

import org.example.shareserver.models.dtos.BattleDTO;
import org.example.shareserver.models.entities.Enemy;
import org.example.shareserver.repositories.BattleRepository;
import org.example.shareserver.repositories.EnemyRepository;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;

@Service
public class OpenCVService {
    @Autowired
    private PhotoStorageService photoStorageService;
    @Autowired
    private BattleRepository battleRepository;

    @Value("${gmaps.key}")
    private String apiKey;
    @Autowired
    private EnemyRepository enemyRepository;

    public ResponseEntity<?> fetchStreetViewImage(double lat, double lng, String id) {
        // Construct Street View URL
        System.out.println(apiKey);
        String urlStr = String.format(
                "https://maps.googleapis.com/maps/api/streetview?size=512x512&location=%f,%f&key=%s",
                lat, lng, apiKey
        );

        URL url = null;
        try {
            url = new URL(urlStr);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try (InputStream in = connection.getInputStream()) {
            byte[] bytes = in.readAllBytes();
            MultipartFile generatedFile = new MockMultipartFile(
                    "image",
                    "result.png",
                    "image/png",
                    bytes
            );
            String blob = photoStorageService.uploadGmapsPhoto(generatedFile, id);
            Optional<Enemy> battle = enemyRepository.findById(id);
            battle.get().setPathToGmapsPhoto(blob);
            return ResponseEntity.status(200).body(photoStorageService.getSignedUrl(blob, BucketType.MOB));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
