package com.example.qnr.security;

import com.example.qnr.services.CustomUserDetailsService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Utility class for handling JWT (JSON Web Token) operations.
 * <p>
 * This class provides methods for generating, validating, and extracting information from JWT tokens.
 * It uses HMAC-SHA256 for signing and validating the JWT token and is responsible for parsing the token's claims.
 * </p>
 */
@Component
public class JwtUtil {

    /**
     * The service used to load user details by username.
     * This service is used for fetching user authorities and other details for generating the JWT token.
     */
    private final CustomUserDetailsService userDetailsService;

    /**
     * The secret key used for signing and validating the JWT token.
     * This key is encoded as a Base64 string for easy transport and storage.
     */
    private String secretKey = "";

    /**
     * The security configuration containing expiration and other settings for JWT token.
     */
    private final SecurityProperties securityConfig;

    /**
     * Constructs a JwtUtil instance and generates a secret key for HMAC-SHA256 signing.
     * The secret key is encoded in Base64 for secure storage and use.
     *
     * @param securityProperties the configuration properties containing JWT-related settings.
     */
    public JwtUtil(CustomUserDetailsService userDetailsService, SecurityProperties securityProperties) {
        this.userDetailsService = userDetailsService;
        this.securityConfig = securityProperties;

        try {
            KeyGenerator keyGen = KeyGenerator.getInstance("HmacSHA256");
            SecretKey sk = keyGen.generateKey();
            secretKey = Base64.getEncoder().encodeToString(sk.getEncoded());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Generates a JWT token for the specified username.
     * <p>
     * The token includes claims such as the subject (username), issued date, and expiration time.
     * The token is signed with the secret key.
     * </p>
     *
     * @param username the username for which the token is generated.
     * @return a JWT token as a string.
     */
    public String generateToken(String username) {
        Map<String, Object> claims = new HashMap<>();

        var users = userDetailsService.loadUserByUsername(username);
        claims.put("roles", users.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList()));

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + securityConfig.getExpiration()))
                .signWith(getKey())
                .compact();
    }

    /**
     * Retrieves the key used for signing and validating JWT tokens.
     * The key is derived from the secret key encoded in Base64.
     *
     * @return the secret key for signing JWT tokens.
     */
    private Key getKey() {
        byte[] keyByte = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyByte);
    }

    /**
     * Extracts the username from the given JWT token.
     * The username is the subject of the token.
     *
     * @param jwToken the JWT token.
     * @return the username extracted from the token.
     */
    public String extractUsername(String jwToken) {
        return extractClaim(jwToken, Claims::getSubject);
    }

    /**
     * Extracts a specific claim from the JWT token.
     * The claim is retrieved by applying the provided function to the claims of the token.
     *
     * @param token the JWT token.
     * @param claimsResolver a function that extracts a specific claim from the claims.
     * @param <T> the type of the claim.
     * @return the extracted claim of type T.
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Extracts the expiration date from the given JWT token.
     *
     * @param token the JWT token.
     * @return the expiration date of the token.
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Checks if the JWT token is expired based on the expiration date.
     *
     * @param token the JWT token.
     * @return true if the token is expired, false otherwise.
     */
    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * Extracts all claims from the JWT token.
     * The claims are parsed from the JWT token using the secret key.
     *
     * @param token the JWT token.
     * @return the claims of the token.
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder().setSigningKey(getKey()).build().parseClaimsJws(token).getBody();
    }

    /**
     * Validates the JWT token by checking if the username matches the one in the token and if the token is not expired.
     *
     * @param token the JWT token.
     * @param userDetails the details of the user to validate against.
     * @return true if the token is valid, false otherwise.
     */
    public boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }
}
