package com.example.qnr.services;

import com.example.qnr.dao.UserRepository;
import com.example.qnr.security.entities.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

/**
 * Custom implementation of the UserDetailsService interface for loading user-specific data.
 * This service interacts with the user repository to fetch user information and implements token blacklisting functionality.
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    // A set to store blacklisted JWT tokens (e.g., for logout functionality).
    private final Set<String> blacklistedTokens = new HashSet<>();

    /**
     * Loads user-specific data by username.
     * This method is used by Spring Security to load user details based on the username.
     *
     * @param username the username of the user to load.
     * @return the UserDetails object representing the user, wrapped in a UserPrincipal.
     * @throws UsernameNotFoundException if no user with the given username exists in the repository.
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Fetch the user from the repository by username
        var user = userRepository.findByUsername(username).orElseThrow(() ->
                new UsernameNotFoundException("User not found"));

        // Return a custom UserPrincipal object containing user details
        return new UserPrincipal(user);
    }

    /**
     * Checks whether a token is blacklisted.
     * This is typically used to check if a JWT token is revoked (e.g., after logout).
     *
     * @param token the JWT token to check.
     * @return true if the token is blacklisted, false otherwise.
     */
    public boolean isTokenBlacklisted(String token) {
        return blacklistedTokens.contains(token);
    }

    /**
     * Adds a token to the blacklist.
     * This method is used to mark a token as blacklisted, preventing it from being used for authentication.
     *
     * @param token the JWT token to blacklist.
     */
    public void blacklistToken(String token) {
        blacklistedTokens.add(token);
    }
}
