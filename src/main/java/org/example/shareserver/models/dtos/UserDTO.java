package org.example.shareserver.models.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.shareserver.models.entities.Item;
import org.springframework.data.annotation.Id;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    @Id
    @NotBlank(message = "User id is required")
    private String id;

    private String nickname;
    private String firstName;
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    private String pathToPhoto;
    private String password;

    private int hp = 100;
    private int damage = 15;
    private int coins = 200;
    private int gems = 10;
    private List<Item> items;
}
