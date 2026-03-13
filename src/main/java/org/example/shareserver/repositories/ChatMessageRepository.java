package org.example.shareserver.repositories;

import org.example.shareserver.models.entities.ChatMessageEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends MongoRepository<ChatMessageEntity, String> {
    @Query("{ $or: [ " +
            " { sender: ?0, receiver: ?1 }," +
            " { sender: ?1, receiver: ?0 } " +
            "] }")
    List<ChatMessageEntity> findPrivateConversation(String userA, String userB);
}
