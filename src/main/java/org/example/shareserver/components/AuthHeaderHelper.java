package org.example.shareserver.components;

import org.example.shareserver.services.JWTService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Single place to resolve user id from Authorization header.
 * Returns Optional.empty() when header is null/empty, missing "Bearer " prefix, or token is invalid.
 */
@Component
public class AuthHeaderHelper {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JWTService jwtService;

    @Autowired
    public AuthHeaderHelper(JWTService jwtService) {
        this.jwtService = jwtService;
    }

    public Optional<String> getUserIdFromAuthHeader(String authorization) {
        if (authorization == null || authorization.isBlank()) {
            return Optional.empty();
        }
        String trimmed = authorization.trim();
        if (!trimmed.startsWith(BEARER_PREFIX)) {
            return Optional.empty();
        }
        String token = trimmed.substring(BEARER_PREFIX.length()).trim();
        if (token.isEmpty()) {
            return Optional.empty();
        }
        try {
            if (!jwtService.validateToken(token)) {
                return Optional.empty();
            }
            String userId = jwtService.getDataFromToken(token);
            return userId != null ? Optional.of(userId) : Optional.empty();
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
