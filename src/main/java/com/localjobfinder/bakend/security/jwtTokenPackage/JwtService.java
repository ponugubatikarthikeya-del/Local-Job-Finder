package com.localjobfinder.bakend.security.jwtTokenPackage;

import com.localjobfinder.bakend.dbEntity.UserEntity;
import com.localjobfinder.bakend.enums.ErrorMsg;
import com.localjobfinder.bakend.exceptionPackage.custom_exceptions.InvalidException;
import com.localjobfinder.bakend.exceptionPackage.custom_exceptions.ResourceNotFoundException;
import com.localjobfinder.bakend.repository.UserRepository;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class JwtService {

    @Value("${jwt.secret.key}")
    private String secretKey;

    private final UserRepository userRepository;

    @Autowired
    public JwtService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // token expiration time
    private static final long REFRESH_TOKEN_EXPIRATION = 1000L * 60 * 60 * 24 * 180; //86400000 * 7 for 7 days
    private static final long ACCESS_TOKEN_EXPIRATION = 1000 * 60 * 60 * 24 ; // 24 hr
//    private static final long ACCESS_TOKEN_EXPIRATION = 1000 * 60 * 2; // 1min hr

    // generate signing key
    private SecretKey getKey() {
        String key = (secretKey != null && secretKey.trim().length() >= 32)
                ? secretKey
                : System.getenv("JWT_SECRET");

        if (key == null || key.trim().length() < 32) {
            throw new InvalidException(
                    "JWT secret key must be at least 32 characters long."
            );
        }

        // Check if the key is Base64 encoded
        try {
            byte[] decoded = java.util.Base64.getDecoder().decode(key);
            if (decoded.length >= 32) {
                return Keys.hmacShaKeyFor(decoded);
            }
        } catch (IllegalArgumentException ignored) {
            // Not a valid Base64 string; fallback to raw bytes
        }

        return Keys.hmacShaKeyFor(key.getBytes(StandardCharsets.UTF_8));
    }

//      private SecretKey generateKey() {
//
//          return Keys.hmacShaKeyFor(secretKey.getBytes());
//}


    // return only important user data

    private Map<String,String> getData(Long userId) {

        Map<String,String> data = new HashMap<>();

        UserEntity userEntity = userRepository.findById(userId)
                .orElseThrow( () -> new ResourceNotFoundException(ErrorMsg.AUTH_008.getMessage()));

        data.put("username",userEntity.getUsername());

        return data;
    }

    // access token generate

    public String generateAccessToken(Long userId) {
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claims()
                .add(getData(userId))
                .and()
                // issued time
                .issuedAt(new Date())
                // expiration time
                .expiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_EXPIRATION))
                // sign token
                .signWith(getKey())
                // build token
                .compact();
    }

    // refresh token generate

    public String generateRefreshToken(Long userId) {
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claims()
                .add(getData(userId))
                .and()
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + REFRESH_TOKEN_EXPIRATION))
                .signWith(getKey())
                .compact();
    }


    // Parse token claims using the latest JJWT standards
    private Claims extractClaims(String token) {
        try {
            return Jwts.parser()                 // 1. Changed from parserBuilder() to parser()
                    .verifyWith(getKey())        // 2. Changed from setSigningKey() to verifyWith()
                    .build()
                    .parseSignedClaims(token)    // 3. Changed from parseClaimsJws() to parseSignedClaims()
                    .getPayload();               // 4. Changed from getBody() to getPayload()
        } catch (JwtException | IllegalArgumentException e) {
            // Token is invalid, expired, or malformed
            throw new InvalidException(ErrorMsg.TOKEN_002.getMessage()); // Invalid token
        }
    }


    public String extractUserId(String token) {
        return extractClaims(token).getSubject();
    }

    // validate the token
    public boolean validateToken(String token, String userId) {
        String extracted = extractUserId(token);
        return extracted.equals(userId) && !isTokenExpired(token);
    }

    public boolean isTokenExpired(String token) {
        try {
            return extractClaims(token).getExpiration().before(new Date());
        } catch (Exception e) {
            return true; // If parsing fails or catches an exception, treat it as expired/invalid
        }
    }

}
