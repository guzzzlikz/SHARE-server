package org.example.shareserver.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.example.shareserver.models.dtos.BattleDTO;
import org.example.shareserver.models.dtos.OpenAIImageDTO;
import org.example.shareserver.models.entities.Enemy;
import org.example.shareserver.repositories.EnemyRepository;
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
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Service
@EnableCaching
public class AiService {
    @Autowired
    private PhotoStorageService photoStorageService;
    @Autowired
    private WebClient webClient;
    @Autowired
    private EnemyRepository enemyRepository;
    @Autowired
    private ObjectMapper objectMapper;

    public ResponseEntity<?> ask(String prompt) {
        if (prompt == null || prompt.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        Map<String, Object> body = Map.of(
                "model", "gpt-4o-mini",
                "messages", List.of(
                        Map.of("role", "system",
                                "content", "You are an AI assistant. If the user asks about products" +
                                        "give him info from database that I provide as well (it is stored in JSON)" +
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

    public ResponseEntity<?> generateProfile(MultipartFile file, String userId) {
        if (file == null || file.isEmpty()) {
            log.info("AI service has been called but file is empty");
            return ResponseEntity.status(400).body("File cannot be empty");
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
        if (response == null || response.getData() == null || response.getData().isEmpty()) {
            return ResponseEntity.status(502).body("AI returned no image data");
        }
        String base64 = response.getData().get(0).getB64_json();
        if (base64 == null || base64.isBlank()) {
            return ResponseEntity.status(502).body("AI returned empty image");
        }
        byte[] imageBytes = Base64.getDecoder().decode(base64);
        MultipartFile generatedFile = new MockMultipartFile(
                "image",
                "result.png",
                "image/png",
                imageBytes
        );
        String blob;
        try {
            blob = photoStorageService.uploadUserProfilePhoto(generatedFile, userId);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (blob == null) {
            return ResponseEntity.status(500).body("Failed to upload generated image");
        }
        log.info("Blob succeeded!");
        return ResponseEntity.ok().body(photoStorageService.getSignedUrl(blob, BucketType.USER));
    }

    public ResponseEntity<?> generateBattlePhoto(MultipartFile file, String userId) {
        if (file == null || file.isEmpty()) {
            log.info("AI service has been called but file is empty");
            return ResponseEntity.status(400).body("File cannot be empty");
        }
        MultiValueMap<String, Object> form = new LinkedMultiValueMap<>();
        form.add("model", "gpt-image-1");
        form.add("prompt", "convert this landscape into anime fantasy location");
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
        if (response == null || response.getData() == null || response.getData().isEmpty()) {
            return ResponseEntity.status(502).body("AI returned no image data");
        }
        String base64 = response.getData().get(0).getB64_json();
        if (base64 == null || base64.isBlank()) {
            return ResponseEntity.status(502).body("AI returned empty image");
        }
        byte[] imageBytes = Base64.getDecoder().decode(base64);
        MultipartFile generatedFile = new MockMultipartFile(
                "image",
                "result.png",
                "image/png",
                imageBytes
        );
        String blob;
        try {
            BattleDTO battleDTO = BattleDTO.builder()
                    .userId(userId)
                    .build();
            blob = photoStorageService.uploadBattlePhoto(generatedFile, battleDTO);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (blob == null) {
            return ResponseEntity.status(500).body("Failed to upload generated image");
        }
        log.info("Blob succeeded!");
        return ResponseEntity.ok().body(photoStorageService.getSignedUrl(blob, BucketType.BATTLE));
    }

    public ResponseEntity<?> check(MultipartFile file, double lat, double lng) {
        if (file == null || file.isEmpty()) {
            return ResponseEntity.status(400).body("File cannot be empty");
        }
        byte[] bytes = null;
        try {
            bytes = file.getBytes();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        String base64 = Base64.getEncoder().encodeToString(bytes);
        String prompt = """
A user claims they are currently at the following GPS location:
Latitude: %f
Longitude: %f
Analyze the provided photo and determine whether the environment in the image plausibly matches this geographic location.
Consider things such as:
- architecture style
- vegetation
- road signs or language
- terrain and landscape
- climate indicators
- visible landmarks
- current weather report
Return your answer in this format:
match_probability: number between 0 and 100
reason: short explanation of why the image does or does not match the location
""".formatted(lat, lng);

        Map<String, Object> requestBody =
                Map.of(
                        "model", "gpt-4.1-mini",
                        "input", new Object[]{
                                Map.of(
                                        "role", "user",
                                        "content", new Object[]{
                                                Map.of(
                                                        "type", "input_text",
                                                        "text", prompt
                                                ),
                                                Map.of(
                                                        "type", "input_image",
                                                        "image_url", "data:image/jpeg;base64," + base64
                                                )
                                        }
                                )
                        }
                );
        String response = webClient.post()
                .uri("/responses")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .block();
        log.info("Response succeeded!");
        JsonNode root;
        try {
            root = objectMapper.readTree(response);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse AI response", e);
        }
        String text = root
                .path("output")
                .get(0)
                .path("content")
                .get(0)
                .path("text")
                .asText();
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("\\d+").matcher(text);
        int probability = m.find() ? Integer.parseInt(m.group()) : 0;
        if (probability > 50) {
            return ResponseEntity.status(200).build();
        }
        return ResponseEntity.status(400).build();
    }

    public ResponseEntity<?> generateChests(String city) {
        if (city == null || city.isBlank()) {
            return ResponseEntity.status(400).body("City is required");
        }
        List<Enemy> list = enemyRepository.findAll().stream().limit(10).collect(Collectors.toList());
        List<Enemy> chests = list.stream().filter(c -> c.getChestType() != null).toList();
        Map<String, Object> body = Map.of(
                "model", "gpt-4o-mini",
                "messages", List.of(
                        Map.of("role", "system",
                                "content", "You are an AI assistant that helps generate positions of enemies. " +
                                        "Your goal is to generate latitude and longtitude" + chests.size() + "times" +
                                        "based on the city that I provide" + city +
                                        "You should give only " +
                                        "latitude:value " +
                                        "longtitude:value " +
                                        "Remember: your answer should not be huge - just few sentences" +
                                        "The longtitude and latitude should be located closer to the center" +
                                        "of the city. Avoid coordinates pointing to houses, point ONLY" +
                                        "at streets!"),
                        Map.of("role", "user",
                                "content", "")
                )
        );
        String prompt = webClient.post()
                .uri("/chat/completions")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(String.class)
                .block();
        JsonNode root;
        try {
            root = objectMapper.readTree(prompt);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse AI response", e);
        }
        String content = root
                .path("choices")
                .get(0)
                .path("message")
                .path("content")
                .asText();
        content = content.toLowerCase();
        Pattern pattern = Pattern.compile("latitude:\\s*([0-9.]+).*?longitude:\\s*([0-9.]+)", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(content);

        List<double[]> coordinates = new ArrayList<>();
        while (matcher.find()) {
            double lat = Double.parseDouble(matcher.group(1));
            double lng = Double.parseDouble(matcher.group(2));
            coordinates.add(new double[]{lat, lng});
        }
        int count = Math.min(coordinates.size(), chests.size());
        List<Enemy> output = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Enemy chest = chests.get(i);
            double[] coord = coordinates.get(i);
            chest.setLatitude(coord[0]);
            chest.setLongitude(coord[1]);
            output.add(chest);
        }
        return ResponseEntity.ok().body(output);
    }
    public ResponseEntity<?> generateStory(String street, String lang) {
        if (street == null || street.isEmpty()) {
            return ResponseEntity.status(400).build();
        }
        String langInstruction = "uk".equalsIgnoreCase(lang)
                ? " Respond in Ukrainian language."
                : " Respond in English language.";
        Map<String, Object> body = Map.of(
                "model", "gpt-4o-mini",
                "messages", List.of(
                        Map.of("role", "system",
                                "content", "You are an AI assistant that helps generate " +
                                        "story based on the street. " +
                                        "Your goal is to generate absolutely truthful story " +
                                        "sourcing through the internet about some interesting events " +
                                        "that have ever happened on this street or the street nearby. " +
                                        "Your story should be not bigger than 2 sentences. Remember," +
                                        "the user SHOULD NOT PROVIDE YOU ANY MESSAGE" +
                                        langInstruction + " Street: " + street),
                        Map.of("role", "user",
                                "content", "")
                )
        );
        String prompt = webClient.post()
                .uri("/chat/completions")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(String.class)
                .block();
        JsonNode root;
        try {
            root = objectMapper.readTree(prompt);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse AI response", e);
        }
        String content = root
                .path("choices")
                .get(0)
                .path("message")
                .path("content")
                .asText();
        return ResponseEntity.status(200).body(content);
    }
    public ResponseEntity<?> generateQuiz(String street) {
        Map<String, Object> body = Map.of(
                "model", "gpt-4o-mini",
                "messages", List.of(
                        Map.of("role", "system",
                                "content", "You are an AI assistant that helps generate " +
                                        "quiz based on the street story. " +
                                        "Your goal is to generate one truthful story (it should start with truth:)" +
                                        "and two fake stories (should start with lie:) (you can create them yourself)" +
                                        "Your story should be not bigger than 3 sentences" +
                                        street),
                        Map.of("role", "user",
                                "content", "")
                )
        );
        String prompt = webClient.post()
                .uri("/chat/completions")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(String.class)
                .block();
        JsonNode root;
        try {
            root = objectMapper.readTree(prompt);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse AI response", e);
        }
        String content = root
                .path("choices")
                .get(0)
                .path("message")
                .path("content")
                .asText();
        return ResponseEntity.status(200).body(content);
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
