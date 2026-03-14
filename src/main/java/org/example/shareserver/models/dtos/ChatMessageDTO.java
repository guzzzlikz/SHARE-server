package org.example.shareserver.models.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageDTO {
    private String sender;

    @NotBlank(message = "Receiver is required")
    private String receiver;

    @NotBlank(message = "Content is required")
    private String content;
}
