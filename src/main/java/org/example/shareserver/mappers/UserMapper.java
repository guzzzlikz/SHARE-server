package org.example.shareserver.mappers;

import org.example.shareserver.models.dtos.LoginDTO;
import org.example.shareserver.models.dtos.UserDTO;
import org.example.shareserver.models.entities.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {

    User toUserFromLogin(LoginDTO loginDTO);
    User toUser(LoginDTO loginDTO);
    LoginDTO toLoginDto(User user);

}
