package org.example.shareserver.repositories;

import org.example.shareserver.models.entities.Enemy;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface EnemyRepository extends MongoRepository<Enemy, String> {
    List<Enemy> findByCity(String city);

    List<Enemy> findByKilledAtBefore(Instant time);
}
