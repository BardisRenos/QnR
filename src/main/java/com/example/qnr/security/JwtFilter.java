package com.example.qnr.security;

import com.example.qnr.services.CustomUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;


/**
 * JWT filter responsible for extracting and validating the JWT token from HTTP requests.
 * <p>
 * This filter intercepts incoming HTTP requests, checks for the presence of a JWT token in the
 * Authorization header, and validates it. If the token is valid, the user is authenticated, and
 * their details are set in the Spring Security context. If the token is invalid or blacklisted,
 * the request is rejected with an Unauthorized status.
 * </p>
 */
@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    /**
     * The utility class used for handling JWT operations like extraction and validation.
     */
    private final JwtUtil jwtUtil;

    /**
     * The security properties used for extracting header details like token prefix.
     */
    private final SecurityProperties securityProperties;

    /**
     * The service used to load user details and check if a token is blacklisted.
     */
    private final CustomUserDetailsService userDetailsService;

    /**
     * Intercepts incoming HTTP requests, extracts the JWT token from the Authorization header,
     * validates the token, and authenticates the user if the token is valid.
     * <p>
     * If the token is valid, the user's details are set in the Spring Security context for
     * subsequent authorization checks. If the token is invalid or blacklisted, the request is
     * rejected with a 401 Unauthorized response.
     * </p>
     *
     * @param request the HTTP request
     * @param response the HTTP response
     * @param filterChain the filter chain
     * @throws ServletException if an error occurs during filter processing
     * @throws IOException if an I/O error occurs during filter processing
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader(securityProperties.getHeaderString());
        String username = null;
        String jwToken = null;

        if (authHeader != null && authHeader.startsWith(securityProperties.getTokenPrefix())) {
            jwToken = authHeader.substring(7);
            username = jwtUtil.extractUsername(jwToken);

        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            if (userDetailsService.isTokenBlacklisted(jwToken)) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Token is invalid");
                return;
            }

            if (jwtUtil.validateToken(jwToken, userDetails)) {
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());

                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }
        filterChain.doFilter(request, response);
    }
}
