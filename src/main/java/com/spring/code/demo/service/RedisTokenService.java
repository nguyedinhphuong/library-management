package com.spring.code.demo.service;

import com.spring.code.demo.exception.ResourceNotFoundException;
import com.spring.code.demo.model.RedisToken;
import com.spring.code.demo.repository.RedisTokenRepository;
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

    public RedisToken getById(String id) {
        return redisTokenRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Redis token not found"));
    }
}
