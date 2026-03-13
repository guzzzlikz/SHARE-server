package org.example.shareserver.models.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection="users")
public class User {
    @Id
    private String id;
    private String firstName;
    private String lastName;
    private String email;
    private String pathToPhoto;
    private String password;
}
