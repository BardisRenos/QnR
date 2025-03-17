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
public class UserDtoNoPass {
    private String username;
    private UserRole role;
}
