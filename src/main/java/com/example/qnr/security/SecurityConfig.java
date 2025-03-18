package com.example.qnr.security;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Security configuration class for Spring Security setup.
 * <p>
 * This class configures security for the application, defining the authentication and authorization mechanisms.
 * It includes configurations for JWT token-based authentication, login/logout functionality, password encoding,
 * session management, and HTTP security settings.
 * </p>
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    /**
     * The service responsible for loading user-specific data during authentication.
     */
    private final UserDetailsService userDetailsService;

    /**
     * The filter that processes JWT tokens during authentication.
     */
    private final JwtFilter jwtFilter;

    /**
     * Configures the HTTP security settings for the application.
     * <p>
     * This method sets up the authorization rules, disables CSRF protection, configures session management
     * (stateless for JWT), and adds the custom JWT filter for token validation.
     * It also defines public endpoints (login, register, and logout) that are accessible without authentication.
     * </p>
     *
     * @param http the HttpSecurity object to configure security settings.
     * @return the SecurityFilterChain for the application's security configuration.
     * @throws Exception if an error occurs during the configuration process.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/v1.0/user/register",
                                "/api/v1.0/user/login",
                                "/api/v1.0/user/logout",
                                "/swagger-ui/index.html"
                        ).permitAll()
                        .anyRequest().authenticated())
                .httpBasic(Customizer.withDefaults())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .logout(logout -> logout
                        .logoutUrl("/api/v1.0/user/logout") // Define the logout URL
                        .logoutSuccessHandler((request, response, authentication) -> {
                            response.setStatus(HttpServletResponse.SC_OK);
                            response.getWriter().write("Logged out successfully");
                            response.getWriter().flush();
                        })
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .deleteCookies("JSESSIONID"));

        return http.build();
    }

    /**
     * Configures the authentication provider used for authenticating users.
     * <p>
     * This method sets up a {@link DaoAuthenticationProvider} that uses a password encoder
     * (BCrypt) and the provided {@link UserDetailsService} for user authentication.
     * </p>
     *
     * @return the configured AuthenticationProvider.
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setPasswordEncoder(new BCryptPasswordEncoder(10)); // Set the password encoder to BCrypt with strength 10
        provider.setUserDetailsService(userDetailsService); // Set the user details service for loading users
        return provider;
    }

    /**
     * Configures the authentication manager.
     * <p>
     * This method retrieves the {@link AuthenticationManager} from the {@link AuthenticationConfiguration}.
     * </p>
     *
     * @param config the AuthenticationConfiguration used to create the AuthenticationManager.
     * @return the AuthenticationManager for handling authentication.
     * @throws Exception if an error occurs during the authentication manager setup.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * Configures the password encoder.
     * <p>
     * This method returns a {@link BCryptPasswordEncoder}, which is used for encoding passwords
     * during the authentication process.
     * </p>
     *
     * @return the BCryptPasswordEncoder for encoding passwords.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
