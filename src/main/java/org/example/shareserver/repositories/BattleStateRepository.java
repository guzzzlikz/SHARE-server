package org.example.shareserver.repositories;

import org.example.shareserver.models.entities.BattleState;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface BattleStateRepository extends MongoRepository<BattleState, String> {
    Optional<BattleState> findByUserIdAndIsOverFalse(String userId);
}
