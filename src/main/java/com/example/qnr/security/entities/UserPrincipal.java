package com.example.qnr.security.entities;

import com.example.qnr.resources.Users;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * Implementation of Spring Security's {@link UserDetails} interface.
 * This class is used to represent the authenticated user's details.
 * It encapsulates the user entity and provides methods for retrieving
 * user information such as authorities, password, and username.
 */
@RequiredArgsConstructor
public class UserPrincipal implements UserDetails {

    private final Users user;

    /**
     * Returns the authorities granted to the user. In this case, a user is granted
     * the "USER" authority.
     *
     * @return a collection of authorities granted to the user.
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        String role = "ROLE_" + user.getRole().toUpperCase();
        return List.of(new SimpleGrantedAuthority(role));
    }

    /**
     * Returns the password of the user.
     *
     * @return the user's password.
     */
    @Override
    public String getPassword() {
        return user.getPassword();
    }

    /**
     * Returns the username of the user.
     *
     * @return the user's username.
     */
    @Override
    public String getUsername() {
        return user.getUsername();
    }
}
