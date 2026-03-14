package org.example.shareserver.services;

import io.jsonwebtoken.security.Jwks;
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
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
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
    private JWTService jwtService;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private WebClient webClient;
    @Autowired
    private EnemyRepository enemyRepository;

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

    public ResponseEntity<?> generateBattlePhoto(MultipartFile file, String token, String battleId, String mobId) {
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
            BattleDTO battleDTO = BattleDTO.builder()
                    .userId(id)
                    .id(battleId)
                    .mobId(mobId)
                    .build();
            blob = photoStorageService.uploadBattlePhoto(generatedFile, battleDTO);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (blob == null) {
            return ResponseEntity.status(401).build();
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
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(response);
        String text = root
                .path("output")
                .get(0)
                .path("content")
                .get(0)
                .path("text")
                .asText();
        int probability = Integer.parseInt(
                text.replaceAll("[^0-9]", "")
        );
        if (probability > 50) {
            return ResponseEntity.status(200).build();
        }
        return ResponseEntity.status(400).build();
    }

    public ResponseEntity<?> generateChests(String city) {
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
        JsonNode root = objectMapper.readTree(prompt);
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
        List<Enemy> output = new ArrayList<>();
        for (int i = 0; i < coordinates.size(); i++) {
            chests.get(i).setLatitude(coordinates.get(i)[0]);
            chests.get(i).setLongitude(coordinates.get(i)[1]);
            output.add(list.get(i));
        }
        return ResponseEntity.ok().body(output);
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
