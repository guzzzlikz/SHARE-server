package org.example.shareserver.services;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.example.shareserver.components.HashComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.spec.SecretKeySpec;
import java.util.Date;

@Component
public class JWTService {
    @Autowired
    private HashComponent hashComponent;

    @Value("${jwt.secret}")
    private String key;
    private final long endTime = 60 * 60 * 10000;
    public String generateToken(String data) {
        return Jwts.builder()
                .setSubject(data)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + endTime))
                .signWith(new SecretKeySpec(hashComponent.hashToByte(key), SignatureAlgorithm.HS256.getJcaName()))
                .compact();
    }
    public String getDataFromToken(String token) {
        return parseClaims(token).getSubject();
    }
    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    private Claims parseClaims(String token) {
        return Jwts.parser()
                .setSigningKey(new SecretKeySpec(hashComponent.hashToByte(key), SignatureAlgorithm.HS256.getJcaName()))
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
    private boolean isExpired(String token) {
        return Jwts.parser()
                .setSigningKey(new SecretKeySpec(hashComponent.hashToByte(key), SignatureAlgorithm.HS256.getJcaName()))
                .build()
                .parseClaimsJws(token)
                .getBody().getExpiration().before(new Date());
    }
}