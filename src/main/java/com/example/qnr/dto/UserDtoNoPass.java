package com.example.qnr.dto;

import com.example.qnr.resources.enums.UserRole;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserDtoNoPass {

    @NotBlank(message = "The username can not be null or empty")
    private String username;
    @NotBlank(message = "The user role can not be null or empty")
    private UserRole role;
}
