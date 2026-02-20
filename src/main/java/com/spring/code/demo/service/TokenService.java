package com.spring.code.demo.service;

import com.spring.code.demo.exception.ResourceNotFoundException;
import com.spring.code.demo.model.Token;
import com.spring.code.demo.repository.TokenRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public record TokenService(TokenRepository tokenRepository) {

    public int save(Token token) {

        // Khong nen luu nhieu record
        Optional<Token> optional = tokenRepository.findByUsername(token.getUsername());

        // neu op ton tai trong db thi chi can update, con neu khong co thi cho phep tao moi
        if(optional.isEmpty()){
            tokenRepository.save(token);
            return token.getId();
        } else {
            Token currentToken = optional.get();
            currentToken.setAccessToken(token.getAccessToken());
            currentToken.setRefreshToken(token.getRefreshToken());
            tokenRepository.save(currentToken);
            return token.getId();
        }
    }

    public String delete(Token token) {
        tokenRepository.delete(token);
        return "Deleted";
    }

    public Token getByUsername(String username) {
        return tokenRepository.findByUsername(username).orElseThrow(() -> new ResourceNotFoundException("Token not exists"));
    }
}
