package com.spring.code.demo.service.impl;

import com.spring.code.demo.dto.request.AddressDTO;
import com.spring.code.demo.dto.request.UserRequestDTO;
import com.spring.code.demo.dto.response.PageResponse;
import com.spring.code.demo.dto.response.UserDetailResponse;
import com.spring.code.demo.exception.ResourceNotFoundException;
import com.spring.code.demo.model.Address;
import com.spring.code.demo.model.User;
import com.spring.code.demo.repository.SearchRepository;
import com.spring.code.demo.repository.UserRepository;
import com.spring.code.demo.service.UserService;
import com.spring.code.demo.utils.UserStatus;
import com.spring.code.demo.utils.UserType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final SearchRepository searchRepository;

    @Override
    public UserDetailsService userDetailsService() {
        return username -> userRepository.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    @Override
    public int addUser(UserRequestDTO requestDTO) {
        System.out.println("Save user to db");

        if (requestDTO.getFirstName().equals("Phuong")) {
            throw new ResourceNotFoundException("Phương và Thảo");
        }
        return 0;
    }

    @Override
    public long saveUser(UserRequestDTO request) {

        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .dateOfBirth(request.getDateOfBirth())
                .gender(request.getGender())
                .phone(request.getPhone())
                .email(request.getEmail())
                .username(request.getUsername())
                .password(request.getPassword())
                .type(UserType.valueOf(request.getType().toUpperCase()))
                .status(request.getStatus())
                .build();
        request.getAddress().forEach(a ->
                user.saveAddress(Address.builder()
                        .apartmentNumber(a.getApartmentNumber())
                        .floor(a.getFloor())
                        .building(a.getBuilding())
                        .streetNumber(a.getStreetNumber())
                        .street(a.getStreet())
                        .city(a.getCity())
                        .country(a.getCountry())
                        .addressType(a.getAddressType())
                        .build())
        );
        userRepository.save(user);
        log.info("User saved");
        return user.getId();
    }

    @Override
    public long saveUser(User user) {
        userRepository.save(user);
        return user.getId();
    }

    @Override
    public void updateUser(long userId, UserRequestDTO request) {
        User user = getUserById(userId);

        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setDateOfBirth(request.getDateOfBirth());
        user.setGender(request.getGender());
        user.setPhone(request.getPhone());
        if (!request.getEmail().equals(user.getEmail())) {
            user.setEmail(request.getEmail());
        }
        user.setUsername(request.getUsername());
        user.setPassword(request.getPassword());
        user.setType(UserType.valueOf(request.getType().toUpperCase()));
        user.setStatus(request.getStatus());
        user.setAddresses(convertToAddress(request.getAddress()));

        userRepository.save(user);
        log.info("Update user successfully");
    }

    @Override
    public void changeStatus(long userId, UserStatus status) {
        User user = getUserById(userId);
        user.setStatus(status);
        userRepository.save(user);
        log.info("Status changed successfully");
    }

    @Override
    public void deleteUser(long userId) {
        User user = getUserById(userId);
        userRepository.delete(user);
        log.info("Status deleted, userId = {} ", userId);
    }

    @Override
    public User getByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Username not found with username+ "+ username));
    }

    @Override
    public User getByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("user not found "));
    }

    @Override
    public UserDetailResponse getUser(long userId) {
        User user = getUserById(userId);
        return UserDetailResponse.builder()
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phone(user.getPhone())
                .email(user.getEmail())
                .build();
    }

    @Override
    public PageResponse<?> getAllUser(int pageNo, int pageSize, String sortBy) {
        int p = 0;
        if (pageNo > 0) {
            p = pageNo - 1;
        }

        List<Sort.Order> sorts = new ArrayList<>();
        if (StringUtils.hasLength(sortBy)) {
            Pattern pattern = Pattern.compile("(\\w*?)(:)(.*)");
            Matcher matcher = pattern.matcher(sortBy);
            if (matcher.find()) {
                if (matcher.group(3).equalsIgnoreCase("asc")) {
                    sorts.add(new Sort.Order(Sort.Direction.ASC, matcher.group(1)));
                } else if (matcher.group(3).equalsIgnoreCase("desc")) {
                    sorts.add(new Sort.Order(Sort.Direction.DESC, matcher.group(1)));
                }
            }
        }

        Pageable pageable = PageRequest.of(p, pageSize, Sort.by(sorts));
        Page<User> users = userRepository.findAll(pageable);
        List<UserDetailResponse> responseUsers = users.stream()
                .map(user->UserDetailResponse.builder()
                        .id(user.getId())
                        .firstName(user.getFirstName())
                        .lastName(user.getLastName())
                        .email(user.getEmail())
                        .phone(user.getPhone())
                        .build()).toList();
        return PageResponse.builder()
                .pageNo(pageNo)
                .pageSize(pageSize)
                .totalPage(users.getTotalPages())
                .items(responseUsers)
                .build();
    }

    @Override
    public PageResponse<?> getAllUserWithSortByMultipleColumns(int pageNo, int pageSize, String... sorts) {
        int p = 0;
        if (pageNo > 0) {
            p = pageNo - 1;
        }
        List<Sort.Order> orders = new ArrayList<>();
        for(String s: sorts) {
            Pattern pattern = Pattern.compile("(\\w*?)(:)(.*)");
            Matcher matcher = pattern.matcher(s);
            if(matcher.find()){
                if(matcher.group(3).equalsIgnoreCase("asc")){
                    orders.add(new Sort.Order(Sort.Direction.ASC, matcher.group(1)));
                }else if(matcher.group(3).equalsIgnoreCase("desc")){
                    orders.add(new Sort.Order(Sort.Direction.DESC, matcher.group(1)));
                }
            }
        }
        Pageable pageable = PageRequest.of(p, pageSize, Sort.by(orders));
        Page<User> users = userRepository.findAll(pageable);
        List<UserDetailResponse> responseUsers = users.stream()
                .map(user->UserDetailResponse.builder()
                        .id(user.getId())
                        .firstName(user.getFirstName())
                        .lastName(user.getLastName())
                        .email(user.getEmail())
                        .phone(user.getPhone())
                        .build()).toList();
        return PageResponse.builder()
                .pageNo(pageNo)
                .pageSize(pageSize)
                .totalPage(users.getTotalPages())
                .items(responseUsers)
                .build();
    }

    @Override
    public PageResponse<?> getAllUserWithSortByColumnsAndSearch(int pageNo, int pageSize,String search, String sortBy) {
        return searchRepository.getAllUserWithSortByColumnsAndSearch(pageNo,pageSize, search, sortBy);
    }

    @Override
    public PageResponse<?> getAdvanceSearchByCriteria(int pageNo, int pageSize, String sortBy, String... search) {
        return searchRepository.getAdvanceSearchByCriteria(pageNo,pageSize,sortBy, search);
    }

    private Set<Address> convertToAddress(Set<AddressDTO> addresses) {
        Set<Address> result = new HashSet<>();
        addresses.forEach(a ->
                result.add(Address.builder()
                        .apartmentNumber(a.getApartmentNumber())
                        .floor(a.getFloor())
                        .building(a.getBuilding())
                        .street(a.getStreet())
                        .streetNumber(a.getStreetNumber())
                        .city(a.getCity())
                        .country(a.getCountry())
                        .addressType(a.getAddressType())
                        .build()));
        return result;
    }

    private User getUserById(long userId) {
        return userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}
