package com.project.library.service;

import com.project.library.model.RedisToken;
import com.project.library.repository.RedisTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RedisTokenService {

    private final RedisTokenRepository redisTokenRepository;

    public String save(RedisToken token) {
        RedisToken result = redisTokenRepository.save(token);
        return result.getId();
    }
    public void delete(String id){
        redisTokenRepository.deleteById(id);
    }
    public RedisToken getByUsername(String username) {
        return redisTokenRepository.findById(username)
                .orElseThrow(() -> new RuntimeException("Token not found in Redis"));
    }
}
