package com.revconnect.revconnect.user.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;

@Service
public class JwtService {

    private final String SECRET =
            "revconnect-secret-key-must-be-at-least-32-characters-long";

    private final SecretKey key =
            Keys.hmacShaKeyFor(SECRET.getBytes());

    public String generateToken(
            Long userId,
            String email,
            String accountType) {

        return Jwts.builder()
                .subject(email)
                .claim("userId", userId)
                .claim("accountType", accountType)
                .issuedAt(new Date())
                .expiration(
                        new Date(
                                System.currentTimeMillis()
                                        + 1000L * 60 * 60 * 24  // 24 hours
                        )
                )
                .signWith(key)
                .compact();
    }
}