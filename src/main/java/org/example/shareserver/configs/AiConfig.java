package org.example.shareserver.configs;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class AiConfig {

    @Value("${ai.key}")
    private String AiKey;

    @Bean
    public WebClient openAiWebClient() {
        System.out.println(AiKey);
        return WebClient.builder()
                .baseUrl("https://api.openai.com/v1")
                .defaultHeader("Authorization", "Bearer " + AiKey)
                .defaultHeader("Content-Type", "application/json")
                .exchangeStrategies(
                        ExchangeStrategies.builder()
                                .codecs(configurer ->
                                        configurer.defaultCodecs()
                                                .maxInMemorySize(10 * 1024 * 1024)
                                )
                                .build()
                )
                .build();
    }
}
