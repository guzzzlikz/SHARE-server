package org.example.shareserver.controllers;

import org.example.shareserver.mappers.MessageMapper;
import org.example.shareserver.models.dtos.ChatMessageDTO;
import org.example.shareserver.models.entities.ChatMessageEntity;
import org.example.shareserver.repositories.ChatMessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.time.LocalDateTime;

@Controller
public class ChatController {

    @Autowired
    private ChatMessageRepository messageRepository;

    @Autowired
    private MessageMapper messageMapper;

    @Autowired
    private SimpMessagingTemplate messageTemplate;

    /**
     * /user/{receiver}/queue/private -> to destination user
     * /user/{sender}/queue/private -> echo of senders own message
     */

    @MessageMapping("/chat.private")
    public void sendPrivateMessage(@Payload ChatMessageDTO chatMessage, Principal principal) {
        String sender = principal.getName();
        chatMessage.setSender(sender);

        saveToDB(chatMessage, chatMessage.getReceiver());

        // Deliver to destination user
        messageTemplate.convertAndSendToUser(chatMessage.getReceiver(), "/queue/private", chatMessage);

        // Echo to sender so he can see own messages
        messageTemplate.convertAndSendToUser(sender, "/queue/private", chatMessage);
    }

    private void saveToDB(ChatMessageDTO msg, String receiver) {
        ChatMessageEntity chatMessageEntity = messageMapper.toEntity(msg);
        chatMessageEntity.setReceiver(receiver);
        chatMessageEntity.setTimestamp(LocalDateTime.now());
        messageRepository.save(chatMessageEntity);
    }
}
