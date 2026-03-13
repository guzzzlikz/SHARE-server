package org.example.shareserver.repositories;

import org.example.shareserver.models.entities.Item;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ItemRepository extends MongoRepository<Item, String> {
}
