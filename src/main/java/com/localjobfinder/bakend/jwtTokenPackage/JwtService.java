package com.localjobfinder.bakend.jwtTokenPackage;

import com.localjobfinder.bakend.enums.ErrorMsg;
import com.localjobfinder.bakend.exceptionPackage.CustomException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
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

    // token expiration time
    private static final long REFRESH_TOKEN_EXPIRATION = 1000 * 60 * 60 * 24 * 7; //86400000 * 7 for 7 days
    private static final long ACCESS_TOKEN_EXPIRATION = 1000 * 60 * 15 ; // 15 min

    // generate signing key
    private SecretKey getKey() {
//        return Keys.hmacShaKeyFor(secretKey.getBytes());
        return Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }

    // return only important user data

    private Map<String,Object> getData(Long userId) {
        Map<String,Object> data = new HashMap<>();
        data.put("id",userId);
//        data.put("name",userEntity.getName());
//        data.put("email",userEntity.getEmail());
//        data.put("phone",userEntity.getPhone());
        return data;
    }

    // access token generate

    public String generateAccessToken(Long userId) {
        return Jwts.builder()

                .subject(String.valueOf(userId))

                .claims(getData(userId))

                // issued time
                .issuedAt(new Date())

                // expiration time
                .expiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_EXPIRATION))

                // sign token
                .signWith(getKey(), SignatureAlgorithm.HS256)

                // build token
                .compact();
    }

    // refresh token generate

    public String generateRefreshToken(Long userId) {
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claims(getData(userId))
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + REFRESH_TOKEN_EXPIRATION))
                .signWith(getKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String extractUserId(String token) {
        return extractClaims(token).getSubject();
    }

    // validate the token
    public boolean validateToken(String token,String userId) {

        String extracted = extractUserId(token);
        return extracted.equals(userId)
                && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractClaims(token).
                getExpiration().
                before(new Date());
    }

    // parse token claims
    private Claims extractClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (Exception e) {
            throw new CustomException(ErrorMsg.TOKEN_002, 401); // Invalid token
        }

    }


}
