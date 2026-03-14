package org.example.shareserver.components;

import org.example.shareserver.services.JWTService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthHeaderHelperTest {

    @Mock
    private JWTService jwtService;

    private AuthHeaderHelper authHeaderHelper;

    @BeforeEach
    void setUp() {
        authHeaderHelper = new AuthHeaderHelper(jwtService);
    }

    @Test
    void getUserIdFromAuthHeader_returns_empty_when_null() {
        assertThat(authHeaderHelper.getUserIdFromAuthHeader(null)).isEmpty();
    }

    @Test
    void getUserIdFromAuthHeader_returns_empty_when_blank() {
        assertThat(authHeaderHelper.getUserIdFromAuthHeader("")).isEmpty();
        assertThat(authHeaderHelper.getUserIdFromAuthHeader("   ")).isEmpty();
    }

    @Test
    void getUserIdFromAuthHeader_returns_empty_when_no_bearer_prefix() {
        assertThat(authHeaderHelper.getUserIdFromAuthHeader("token")).isEmpty();
        assertThat(authHeaderHelper.getUserIdFromAuthHeader("BearerToken")).isEmpty();
    }

    @Test
    void getUserIdFromAuthHeader_returns_empty_when_invalid_token() {
        when(jwtService.validateToken("bad")).thenReturn(false);
        assertThat(authHeaderHelper.getUserIdFromAuthHeader("Bearer bad")).isEmpty();
    }

    @Test
    void getUserIdFromAuthHeader_returns_userId_when_valid() {
        when(jwtService.validateToken("valid-token")).thenReturn(true);
        when(jwtService.getDataFromToken("valid-token")).thenReturn("user123");
        Optional<String> result = authHeaderHelper.getUserIdFromAuthHeader("Bearer valid-token");
        assertThat(result).hasValue("user123");
    }

    @Test
    void getUserIdFromAuthHeader_strips_whitespace_after_bearer() {
        when(jwtService.validateToken("t")).thenReturn(true);
        when(jwtService.getDataFromToken("t")).thenReturn("u1");
        assertThat(authHeaderHelper.getUserIdFromAuthHeader("Bearer  t")).hasValue("u1");
    }
}
