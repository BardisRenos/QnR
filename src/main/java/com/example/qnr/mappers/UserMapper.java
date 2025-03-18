package com.example.qnr.mappers;

import com.example.qnr.dto.UserDto;
import com.example.qnr.dto.UserDtoNoPass;
import com.example.qnr.resources.Users;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

/**
 * Service class responsible for mapping between User entities and User DTOs.
 * This class utilizes ModelMapper to convert between Users and UserDto objects.
 */
@Service
public class UserMapper {

    /**
     * Converts a Users entity to a UserDto.
     *
     * @param user the Users entity to be converted.
     * @return a UserDto that corresponds to the given Users entity.
     */
    public UserDto toUserDto(Users user) {
        return new ModelMapper().map(user, UserDto.class);
    }

    /**
     * Converts a Users entity to a UserDtoNoPass, omitting the password field.
     *
     * @param user the Users entity to be converted.
     * @return a UserDtoNoPass that corresponds to the given Users entity, excluding the password.
     */
    public UserDtoNoPass toUserDtoNoPass(Users user) {
        return new ModelMapper().map(user, UserDtoNoPass.class);
    }

    /**
     * Converts a UserDto to a Users entity.
     *
     * @param userDto the UserDto to be converted.
     * @return a Users entity that corresponds to the given UserDto.
     */
    public Users toUsers(UserDto userDto) {
        return new ModelMapper().map(userDto, Users.class);
    }
}
