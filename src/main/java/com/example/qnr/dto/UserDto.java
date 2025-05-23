package com.example.qnr.dto;

import com.example.qnr.resources.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserDto extends UserDtoNoPass {
    private String password;

    public UserDto(String username, UserRole role, String password) {
        super(username, role);
        this.password = password;
    }
}