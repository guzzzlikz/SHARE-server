package org.example.shareserver.services;

import io.jsonwebtoken.security.Jwks;
import lombok.extern.slf4j.Slf4j;
import org.example.shareserver.models.dtos.OpenAIImageDTO;
import org.example.shareserver.models.entities.Product;
import org.example.shareserver.repositories.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;
import tools.jackson.databind.ObjectMapper;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@EnableCaching
public class AiService {
    @Autowired
    private PhotoStorageService photoStorageService;
    @Autowired
    private JWTService jwtService;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private WebClient webClient;

    public ResponseEntity<?> ask(String prompt) {
        String dbData = giveDbToAi();
        System.out.println(dbData);
        if (prompt == null || prompt.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        Map<String, Object> body = Map.of(
                "model", "gpt-4o-mini",
                "messages", List.of(
                        Map.of("role", "system",
                                "content", "You are an AI assistant. If the user asks about products" +
                                        "give him info from database that I provide as well (it is stored in JSON)"
                                        + "DATABASE:" + dbData +
                                        "You should give only " +
                                        "- description, " +
                                        "- title, " +
                                        "- price." +
                                        "Remember: your answer should not be huge - just few sentences" +
                                        "You should answer ONLY in the same language that user asks you"),
                        Map.of("role", "user",
                                "content", prompt)
                )
        );
        return ResponseEntity.accepted().body(webClient.post()
                .uri("/chat/completions")
                .bodyValue(body)
                .retrieve()
                .bodyToMono(String.class)
                .block());
    }
    @Cacheable("products-json")
    public String giveDbToAi() {
        List<Product> products = productRepository.findAll();
        return objectMapper.writeValueAsString(products);
    }

    public ResponseEntity<?> generateProfile(MultipartFile file, String token) {
        if (file == null || file.isEmpty()) {
            log.info("AI service has been called but file is empty");
            return ResponseEntity.status(400).body("File cannot be empty");
        }
        token = token.replace("Bearer ", "");
        // Convert file to Base64
        String base64File = null;
        try {
            base64File = Base64.getEncoder().encodeToString(file.getBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        MultiValueMap<String, Object> form = new LinkedMultiValueMap<>();

        form.add("model", "gpt-image-1");
        form.add("prompt", "convert this photo into anime style profile picture");
        form.add("size", "1024x1024");

        try {
            form.add("image", new MultipartInputStreamFileResource(
                    file.getInputStream(),
                    file.getOriginalFilename()
            ));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        log.info("Image added - sending request");

        OpenAIImageDTO response = webClient.post()
                .uri("/images/edits")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .bodyValue(form)
                .retrieve()
                .bodyToMono(OpenAIImageDTO.class)
                .block();
        log.info("Request succeeded!");
        String base64 = response.getData().get(0).getB64_json();
        byte[] imageBytes = Base64.getDecoder().decode(base64);
        String id = jwtService.getDataFromToken(token);
        MultipartFile generatedFile = new MockMultipartFile(
                "image",
                "result.png",
                "image/png",
                imageBytes
        );
        String blob = null;
        try {
            blob = photoStorageService.uploadUserProfilePhoto(generatedFile, id);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (blob == null) {
            return ResponseEntity.status(401).build();
        }
        log.info("Blob succeeded!");
        return ResponseEntity.ok().body(photoStorageService.getSignedUrl(blob, BucketType.USER));
    }
}
class MultipartInputStreamFileResource extends InputStreamResource {

    private final String filename;

    public MultipartInputStreamFileResource(InputStream inputStream, String filename) {
        super(inputStream);
        this.filename = filename;
    }

    @Override
    public String getFilename() {
        return filename;
    }

    @Override
    public long contentLength() {
        return -1;
    }
}
