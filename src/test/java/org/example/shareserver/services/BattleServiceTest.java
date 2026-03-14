package org.example.shareserver.services;

import org.example.shareserver.models.entities.Item;
import org.example.shareserver.models.entities.User;
import org.example.shareserver.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BattleServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PhotoStorageService photoStorageService;

    @InjectMocks
    private BattleService battleService;

    @Test
    void generateBattle_returns_404_when_user_not_found() {
        when(userRepository.findById("unknown")).thenReturn(Optional.empty());

        ResponseEntity<?> res = battleService.generateBattle("unknown", "unknown");

        assertThat(res.getStatusCode().value()).isEqualTo(404);
        assertThat(res.getBody()).asString().contains("User not found");
    }

    @Test
    void generateBattle_success_when_user_has_items() {
        User user = new User();
        user.setId("u1");
        user.setHp(100);
        user.setDamage(20);
        Item item = new Item();
        item.setHp(10);
        item.setDamage(5);
        user.setItems(List.of(item));
        when(userRepository.findById("u1")).thenReturn(Optional.of(user));

        ResponseEntity<?> res = battleService.generateBattle("u1", "e1");

        assertThat(res.getStatusCode().value()).isEqualTo(200);
        assertThat(user.getHp()).isEqualTo(110);
        assertThat(user.getDamage()).isEqualTo(25);
        verify(userRepository).save(user);
    }

    @Test
    void generateBattle_success_when_user_has_null_items() {
        User user = new User();
        user.setId("u1");
        user.setHp(100);
        user.setDamage(20);
        user.setItems(null);
        when(userRepository.findById("u1")).thenReturn(Optional.of(user));

        ResponseEntity<?> res = battleService.generateBattle("u1", "e1");

        assertThat(res.getStatusCode().value()).isEqualTo(200);
        assertThat(user.getHp()).isEqualTo(100);
        assertThat(user.getDamage()).isEqualTo(20);
        verify(userRepository).save(user);
    }

    @Test
    void end_returns_404_when_user_not_found() {
        when(userRepository.findById("unknown")).thenReturn(Optional.empty());

        ResponseEntity<?> res = battleService.end("unknown");

        assertThat(res.getStatusCode().value()).isEqualTo(404);
    }

    @Test
    void end_success_and_restores_hp_atk() {
        User user = new User();
        user.setId("u1");
        user.setHp(50);
        user.setDamage(30);
        user.setItems(Collections.emptyList());
        when(userRepository.findById("u1")).thenReturn(Optional.of(user));

        ResponseEntity<?> res = battleService.end("u1");

        assertThat(res.getStatusCode().value()).isEqualTo(200);
        verify(userRepository).save(user);
        verify(photoStorageService).removeBattlePhoto("u1");
    }
}
