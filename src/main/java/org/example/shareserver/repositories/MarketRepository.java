package org.example.shareserver.repositories;

import org.example.shareserver.models.dtos.ItemMarketDTO;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface MarketRepository extends MongoRepository<ItemMarketDTO, String> {
}
