package com.example.qnr.mappers;

import com.example.qnr.dto.UserDto;
import com.example.qnr.dto.UserDtoNoPass;
import com.example.qnr.resources.Users;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

@Service
public class UserMapper {

    public UserDto toUserDto(Users user) {
        return new ModelMapper().map(user, UserDto.class);
    }

    public UserDtoNoPass toUserDtoNoPass(Users user) {
        return new ModelMapper().map(user, UserDtoNoPass.class);
    }

    public Users toUsers(UserDto userDto) {
        return new ModelMapper().map(userDto, Users.class);
    }
}
