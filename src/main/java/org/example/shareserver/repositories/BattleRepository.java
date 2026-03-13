package org.example.shareserver.repositories;

import org.example.shareserver.models.dtos.BattleDTO;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BattleRepository extends MongoRepository<BattleDTO, String> {
}
