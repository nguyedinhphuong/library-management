package com.spring.code.demo.service;

import com.spring.code.demo.dto.request.UserRequestDTO;
import com.spring.code.demo.dto.response.PageResponse;
import com.spring.code.demo.dto.response.UserDetailResponse;
import com.spring.code.demo.model.User;
import com.spring.code.demo.utils.UserStatus;
import org.springframework.security.core.userdetails.UserDetailsService;


public interface UserService {

    UserDetailsService userDetailsService();
    int addUser(UserRequestDTO requestDTO);
    long saveUser(UserRequestDTO request);
    long saveUser(User user);
    void updateUser(long userId, UserRequestDTO request);
    void changeStatus(long userId, UserStatus status);
    void deleteUser(long userId);

    User getByUsername(String username);
    User getByEmail(String email);

    UserDetailResponse getUser(long userId);
    PageResponse<?> getAllUser(int pageNo, int pageSize, String sortBy);
    PageResponse<?> getAllUserWithSortByMultipleColumns(int pageNo, int pageSize, String... sorts);
    PageResponse<?> getAllUserWithSortByColumnsAndSearch(int pageNo, int pageSize, String search, String sortBy);
    PageResponse<?> getAdvanceSearchByCriteria(int pageNo, int pageSize, String sortBy, String... search);
}
