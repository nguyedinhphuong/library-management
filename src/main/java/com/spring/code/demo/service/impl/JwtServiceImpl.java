package com.spring.code.demo.service.impl;

import com.spring.code.demo.exception.InvalidDataException;
import com.spring.code.demo.service.JwtService;
import com.spring.code.demo.utils.TokenType;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import static com.spring.code.demo.utils.TokenType.*;

@Service
public class JwtServiceImpl implements JwtService {

    @Value("${jwt.expiryDay}")
    private long expiryDay;

    @Value("${jwt.expiryHour}")
    private long expiryHour;

    @Value("${jwt.secretKey}")
    private String secretKey;

    @Value("${jwt.refreshKey}")
    private String refreshKey;

    @Value("${jwt.resetKey}")
    private String resetKey;

    @Override
    public String generateToken(UserDetails user) {
        return generateToken(new HashMap<>(), user);
    }

    @Override
    public String generateRefreshToken(UserDetails user) {
        return generateRefreshToken(new HashMap<>(), user);
    }

    @Override
    public String generateResetToken(UserDetails user) {
        return generateResetToken(new HashMap<>(), user);
    }



    @Override
    public String extractUsername(String token, TokenType type) {
        return extractClaim(token,type, Claims::getSubject);
    }

    @Override
    public boolean inValid(String token,TokenType type, UserDetails userDetails) {
        final String username = extractUsername(token, type);
        return (username.equals(userDetails.getUsername()) && !isTokenExpiredToken(token, type));
    }

    private boolean isTokenExpiredToken(String token, TokenType type) {
        return extractExpiration(token, type).before(new Date());
    }
    private Date extractExpiration(String token, TokenType type){
        return extractClaim(token,type, Claims::getExpiration);
    }

    private String generateToken(Map<String, Object> claims, UserDetails userDetails) {
        long expirationMillis = expiryHour * 60 * 60 * 1000;
        Date now = new Date();

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + expirationMillis))
                .signWith(getKey(ACCESS_TOKEN), SignatureAlgorithm.HS256)
                .compact();
    }

    private String generateRefreshToken(Map<String, Object> claims, UserDetails userDetails) {
        long expirationMillis = expiryDay * 24 * 60 * 60 * 1000;
        Date now = new Date();

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + expirationMillis))
                .signWith(getKey(REFRESH_TOKEN), SignatureAlgorithm.HS256)
                .compact();
    }
    private  String generateResetToken(Map<String, Object> claims, UserDetails userDetails) {
        long expirationMillis = 1000 * 60 * 60;
        Date now = new Date();

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + expirationMillis))
                .signWith(getKey(RESET_TOKEN), SignatureAlgorithm.HS256)
                .compact();
    }

    private Key getKey(TokenType type) {
        switch (type) {
            case ACCESS_TOKEN -> {return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey));}
            case REFRESH_TOKEN -> {return Keys.hmacShaKeyFor(Decoders.BASE64.decode(refreshKey));}
            case RESET_TOKEN -> {return Keys.hmacShaKeyFor(Decoders.BASE64.decode(resetKey));}
            default -> throw new InvalidDataException("Token type invalid: " + type);
        }
    }


    private <T> T extractClaim(String token,TokenType type, Function<Claims, T> claimResolver) {
        final Claims claims = extraAllClaims(token, type);
        return claimResolver.apply(claims);
    }

    private Claims extraAllClaims(String token, TokenType type) {
        return Jwts.parserBuilder().setSigningKey(getKey(type)).build().parseClaimsJws(token).getBody();
    }
}
