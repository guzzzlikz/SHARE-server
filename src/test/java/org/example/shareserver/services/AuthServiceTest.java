package org.example.shareserver.services;

import org.example.shareserver.components.HashComponent;
import org.example.shareserver.models.dtos.RegisterDTO;
import org.example.shareserver.models.entities.User;
import org.example.shareserver.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private HashComponent hashComponent;
    @Mock
    private JWTService jwtService;

    @InjectMocks
    private AuthService authService;

    private RegisterDTO validRegisterDTO;

    @BeforeEach
    void setUp() {
        validRegisterDTO = RegisterDTO.builder()
                .id("user1")
                .nickname("TestUser")
                .email("test@example.com")
                .password("password123")
                .build();
        when(hashComponent.hash(anyString())).thenReturn("hashed");
        when(jwtService.generateToken(anyString())).thenReturn("jwt-token");
    }

    @Test
    void register_fails_when_email_already_registered() {
        when(userRepository.findByEmail(validRegisterDTO.getEmail())).thenReturn(new User());

        ResponseEntity<?> res = authService.register(validRegisterDTO);

        assertThat(res.getStatusCode().value()).isEqualTo(400);
        assertThat(res.getBody()).asString().contains("User is already registered");
        verify(userRepository, never()).save(any());
    }

    @Test
    void register_success_when_valid() {
        when(userRepository.findByEmail(validRegisterDTO.getEmail())).thenReturn(null);

        ResponseEntity<?> res = authService.register(validRegisterDTO);

        assertThat(res.getStatusCode().value()).isEqualTo(200);
        verify(userRepository).save(any(User.class));
        verify(jwtService).generateToken("user1");
    }

    @Test
    void login_fails_when_email_empty() {
        ResponseEntity<?> res = authService.login("", "pass");

        assertThat(res.getStatusCode().value()).isEqualTo(400);
        assertThat(res.getBody()).asString().contains("Email is required");
    }

    @Test
    void login_fails_when_password_empty() {
        ResponseEntity<?> res = authService.login("a@b.com", "");

        assertThat(res.getStatusCode().value()).isEqualTo(400);
        assertThat(res.getBody()).asString().contains("Password is required");
    }

    @Test
    void login_fails_when_user_not_found() {
        when(userRepository.findByEmail("unknown@example.com")).thenReturn(null);

        ResponseEntity<?> res = authService.login("unknown@example.com", "pass");

        assertThat(res.getStatusCode().value()).isEqualTo(400);
        assertThat(res.getBody()).asString().contains("User not found");
    }

    @Test
    void login_fails_when_password_incorrect() {
        User existing = new User();
        existing.setId("id1");
        existing.setEmail("u@example.com");
        existing.setPassword("hashed");
        when(userRepository.findByEmail("u@example.com")).thenReturn(existing);
        when(hashComponent.hash("wrong")).thenReturn("wrong-hash");

        ResponseEntity<?> res = authService.login("u@example.com", "wrong");

        assertThat(res.getStatusCode().value()).isEqualTo(400);
        assertThat(res.getBody()).asString().contains("Incorrect password");
    }

    @Test
    void login_success_when_credentials_valid() {
        User existing = new User();
        existing.setId("id1");
        existing.setEmail("u@example.com");
        existing.setPassword("hashed");
        existing.setNickname("User");
        when(userRepository.findByEmail("u@example.com")).thenReturn(existing);
        when(hashComponent.hash("pass")).thenReturn("hashed");
        when(jwtService.generateToken("id1")).thenReturn("token");

        ResponseEntity<?> res = authService.login("u@example.com", "pass");

        assertThat(res.getStatusCode().value()).isEqualTo(200);
        assertThat(existing.getPassword()).isNull();
        verify(jwtService).generateToken("id1");
    }
}
