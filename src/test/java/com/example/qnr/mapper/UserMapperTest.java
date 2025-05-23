package com.example.qnr.mapper;

import com.example.qnr.dto.UserDto;
import com.example.qnr.resources.Users;
import com.example.qnr.mappers.UserMapper;
import com.example.qnr.resources.enums.UserRole;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class UserMapperTest {

    @Test
    void toOrderDto_ShouldMapUserToUserDto_WithSuccess() {
        Users user = new Users(1, "john_doe", "ADMIN", "encodedPassword");

        UserMapper userMapper = new UserMapper();
        UserDto userDto = userMapper.toUserDto(user);

        assertThat(userDto).isNotNull();
        assertThat(userDto.getUsername()).isEqualTo("john_doe");
        assertThat(userDto.getPassword()).isEqualTo("encodedPassword");
        assertThat(userDto.getRole().toString()).isEqualTo("ADMIN");
    }

    @Test
    void toOrders_ShouldMapUserDtoToUser_WithSuccess() {
        UserDto userDto = new UserDto("john_doe", UserRole.ADMIN,"encodedPassword");

        UserMapper userMapper = new UserMapper();
        Users user = userMapper.toUsers(userDto);

        assertThat(user).isNotNull();
        assertThat(user.getUsername()).isEqualTo("john_doe");
        assertThat(user.getPassword()).isEqualTo("encodedPassword");
        assertThat(user.getRole()).isEqualTo("ADMIN");
    }
}
