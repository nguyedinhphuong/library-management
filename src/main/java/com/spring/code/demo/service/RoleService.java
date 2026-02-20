package com.spring.code.demo.service;

import com.spring.code.demo.model.Role;
import com.spring.code.demo.repository.RoleRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public record RoleService(RoleRepository roleRepository) {

    @PostConstruct
    public List<Role> findAll(){
        List<Role> roles = roleRepository.findAll();
        return roles;
    }
}
