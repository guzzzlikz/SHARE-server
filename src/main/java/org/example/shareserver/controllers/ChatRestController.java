package org.example.shareserver.controllers;

import org.example.shareserver.models.entities.ChatMessageEntity;
import org.example.shareserver.repositories.ChatMessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/chat")
public class ChatRestController {
    @Autowired
    private ChatMessageRepository messageRepository;

    @GetMapping("/messages/private")
    public List<ChatMessageEntity> getPrivateHistory(@RequestParam String with, Principal principal){
        String userA = principal.getName();
        return messageRepository.findPrivateConversation(userA, with);
    }
}
