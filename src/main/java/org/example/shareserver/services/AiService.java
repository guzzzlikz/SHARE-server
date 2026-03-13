package org.example.shareserver.services;

import org.example.shareserver.models.entities.Product;
import org.example.shareserver.repositories.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;

@Service
@EnableCaching
public class AiService {
    @Autowired
    ObjectMapper objectMapper;
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
}
