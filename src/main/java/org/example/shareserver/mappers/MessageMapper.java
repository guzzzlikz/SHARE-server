package org.example.shareserver.mappers;

import org.example.shareserver.models.dtos.ChatMessageDTO;
import org.example.shareserver.models.entities.ChatMessageEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface MessageMapper {
    ChatMessageEntity toEntity(ChatMessageDTO chatMessageDto);
}