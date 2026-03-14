package org.example.shareserver.services;

import org.example.shareserver.components.HashComponent;
import org.example.shareserver.models.dtos.UserDTO;
import org.example.shareserver.models.entities.User;
import org.example.shareserver.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private HashComponent hashComponent;
    @Mock
    private JWTService jwtService;

    @InjectMocks
    private UserService userService;

    @Test
    void changeProfileInfo_returns_404_when_user_not_found() {
        UserDTO dto = UserDTO.builder().id("missing").email("a@b.com").build();
        when(userRepository.findById("missing")).thenReturn(Optional.empty());

        ResponseEntity<?> res = userService.changeProfileInfo(dto);

        assertThat(res.getStatusCode().value()).isEqualTo(404);
        assertThat(res.getBody()).asString().contains("User not found");
    }

    @Test
    void changeProfileInfo_success_and_hashes_password_when_provided() {
        User existing = new User();
        existing.setId("u1");
        existing.setNickname("Old");
        existing.setEmail("old@example.com");
        existing.setPassword("oldHash");
        UserDTO dto = UserDTO.builder()
                .id("u1")
                .nickname("NewNick")
                .email("new@example.com")
                .password("newPass")
                .build();
        when(userRepository.findById("u1")).thenReturn(Optional.of(existing));
        when(hashComponent.hash("newPass")).thenReturn("newHash");

        ResponseEntity<?> res = userService.changeProfileInfo(dto);

        assertThat(res.getStatusCode().value()).isEqualTo(200);
        assertThat(existing.getNickname()).isEqualTo("NewNick");
        assertThat(existing.getEmail()).isEqualTo("new@example.com");
        assertThat(existing.getPassword()).isEqualTo("newHash");
        verify(userRepository).save(existing);
    }

    @Test
    void changeProfileInfo_does_not_overwrite_password_when_blank() {
        User existing = new User();
        existing.setId("u1");
        existing.setPassword("existingHash");
        UserDTO dto = UserDTO.builder().id("u1").email("a@b.com").password("").build();
        when(userRepository.findById("u1")).thenReturn(Optional.of(existing));

        ResponseEntity<?> res = userService.changeProfileInfo(dto);

        assertThat(res.getStatusCode().value()).isEqualTo(200);
        assertThat(existing.getPassword()).isEqualTo("existingHash");
    }

    @Test
    void changeItems_returns_403_when_no_bearer_token() {
        ResponseEntity<?> res = userService.changeItems(null, java.util.List.of());
        assertThat(res.getStatusCode().value()).isEqualTo(403);

        res = userService.changeItems("", java.util.List.of());
        assertThat(res.getStatusCode().value()).isEqualTo(403);

        res = userService.changeItems("InvalidFormat", java.util.List.of());
        assertThat(res.getStatusCode().value()).isEqualTo(403);
    }

    @Test
    void changeItems_returns_404_when_user_not_found() {
        when(jwtService.getDataFromToken("t")).thenReturn("missing");
        when(userRepository.findById("missing")).thenReturn(Optional.empty());

        ResponseEntity<?> res = userService.changeItems("Bearer t", java.util.List.of());

        assertThat(res.getStatusCode().value()).isEqualTo(404);
    }

    @Test
    void changeItems_success_when_valid_token() {
        User user = new User();
        user.setId("u1");
        user.setItems(java.util.Collections.emptyList());
        when(jwtService.getDataFromToken("t")).thenReturn("u1");
        when(userRepository.findById("u1")).thenReturn(Optional.of(user));

        ResponseEntity<?> res = userService.changeItems("Bearer t", java.util.List.of());

        assertThat(res.getStatusCode().value()).isEqualTo(200);
        verify(userRepository).save(user);
    }
}
