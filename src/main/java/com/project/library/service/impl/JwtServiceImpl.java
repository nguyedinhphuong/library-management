package com.project.library.service.impl;

import com.project.library.model.User;
import com.project.library.service.JwtService;
import com.project.library.utils.TokenType;
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

import static com.project.library.utils.TokenType.ACCESS_TOKEN;
import static com.project.library.utils.TokenType.REFRESH_TOKEN;


@Service
public class JwtServiceImpl implements JwtService {

    @Value("${jwt.expiryHour}")
    private long expiryHour;

    @Value("${jwt.expiryDay}")
    private long expiryDay;

    @Value("${jwt.secretKey}")
    private String secretKey;

    @Value("${jwt.refreshKey}")
    private String refreshKey;

    @Override
    public String generateToken(UserDetails user) {
        return generateAccessToken(new HashMap<>(), user);
    }

    @Override
    public String extractUsername(String token, TokenType type) {
        return extractClaim(token, Claims::getSubject, type);
    }

    @Override
    public boolean isValid(String token, UserDetails userDetails, TokenType type) {
        final String username = extractUsername(token, type);
        return (username.equals(userDetails.getUsername())) && !isTokenExpiration(token, type);
    }


    @Override
    public String generateRefreshToken(User user) {
        return generateRefreshToken(new HashMap<>(), user);
    }
    private String buildToken(Map<String, Object> claims, UserDetails userDetails, long expiration, TokenType type){
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis()+expiration))
                .signWith(getKey(type), SignatureAlgorithm.HS256)
                .compact();
    }
    private String generateAccessToken(Map<String, Object> claims, UserDetails userDetails) {
        return buildToken(claims, userDetails,1000 * 60 * 60 * expiryHour, ACCESS_TOKEN); // để 1 tiếng
    }
    private String generateRefreshToken(Map<String, Object> claims, UserDetails userDetails) {
        return buildToken(claims, userDetails, 1000 * 60 * 60 * 24 * expiryDay, REFRESH_TOKEN); // để 1 tiếng
    }
    private Key getKey(TokenType type) {
        byte[] keyBytes;
        if(ACCESS_TOKEN.equals(type)){
            keyBytes = Decoders.BASE64.decode(secretKey);
        }else if(REFRESH_TOKEN.equals(type)){
            keyBytes = Decoders.BASE64.decode(refreshKey);
        } else {
            throw new IllegalArgumentException("Invalid token type");
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }
    public <T> T extractClaim(String token, Function<Claims, T> claimResolver, TokenType type) {
        final Claims claims = extractAllClaims(token, type);
        return claimResolver.apply(claims);
    }

    private Claims extractAllClaims(String token, TokenType type) {
        return Jwts.parserBuilder().setSigningKey(getKey(type)).build().parseClaimsJws(token).getBody();
    }
    private boolean isTokenExpiration(String token, TokenType type) {
        return  extractExpiration(token, type).before(new Date());
    }
    private Date extractExpiration(String token, TokenType type) {
        return extractClaim(token, Claims::getExpiration, type);
    }

}
