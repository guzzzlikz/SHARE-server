package org.example.shareserver.services;

import org.example.shareserver.models.entities.Enemy;
import org.example.shareserver.models.entities.User;
import org.example.shareserver.repositories.EnemyRepository;
import org.example.shareserver.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EnemyServiceTest {

    @Mock
    private EnemyRepository enemyRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private EnemyService enemyService;

    @Test
    void killEnemyById_returns_404_when_enemy_not_found() {
        when(enemyRepository.findById("e1")).thenReturn(Optional.empty());

        ResponseEntity<?> res = enemyService.killEnemyById("e1", "user1");

        assertThat(res.getStatusCode().value()).isEqualTo(404);
        assertThat(res.getBody()).asString().contains("Enemy not found");
    }

    @Test
    void killEnemyById_returns_404_when_user_not_found() {
        when(enemyRepository.findById("e1")).thenReturn(Optional.of(new Enemy()));
        when(userRepository.findById("user1")).thenReturn(Optional.empty());

        ResponseEntity<?> res = enemyService.killEnemyById("e1", "user1");

        assertThat(res.getStatusCode().value()).isEqualTo(404);
        assertThat(res.getBody()).asString().contains("User not found");
    }

    @Test
    void killEnemyById_success_and_updates_user_coins_xp() {
        Enemy enemy = new Enemy();
        enemy.setBoss(false);
        User user = new User();
        user.setId("user1");
        user.setCoins(10);
        user.setGems(0);
        user.setXp(0);
        when(enemyRepository.findById("e1")).thenReturn(Optional.of(enemy));
        when(userRepository.findById("user1")).thenReturn(Optional.of(user));

        ResponseEntity<?> res = enemyService.killEnemyById("e1", "user1");

        assertThat(res.getStatusCode().value()).isEqualTo(200);
        assertThat(user.getCoins()).isEqualTo(25);
        assertThat(user.getGems()).isEqualTo(0);
        assertThat(user.getXp()).isEqualTo(25);
        verify(enemyRepository).save(enemy);
        verify(userRepository).save(user);
    }
}
