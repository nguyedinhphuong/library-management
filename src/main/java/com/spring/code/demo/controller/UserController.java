package com.spring.code.demo.controller;


import com.spring.code.demo.dto.request.UserRequestDTO;
import com.spring.code.demo.dto.response.ResponseData;
import com.spring.code.demo.dto.response.ResponseError;
import com.spring.code.demo.dto.response.UserDetailResponse;
import com.spring.code.demo.exception.ResourceNotFoundException;
import com.spring.code.demo.service.UserService;
import com.spring.code.demo.utils.UserStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/user")
@Validated
@Slf4j
@Tag(name = "User Controller")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "Add user", description = "API create new user")
    @PostMapping("/")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseData<Long> addUser(@Valid @RequestBody UserRequestDTO user) {
        try {
            long userId = userService.saveUser(user);
            return new ResponseData<>(HttpStatus.CREATED.value(), "User add success", userId);
        } catch (Exception e) {
            log.error("errorMessage = {} ", e.getMessage(), e.getCause());
            return new ResponseError(HttpStatus.BAD_REQUEST.value(), "Save user fail !!");
        }
    }

    @Operation(summary = "Update user", description = "API update user")
    @PutMapping("/{userId}")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public ResponseData<Void> updateUser(@PathVariable @Min(1) int userId, @Valid @RequestBody UserRequestDTO user) {
        try {
            userService.updateUser(userId, user);
            return new ResponseData<>(
                    HttpStatus.ACCEPTED.value(),
                    "User updated successfully");
        } catch (NoSuchElementException e) {
            return new ResponseData<>(
                    HttpStatus.NOT_FOUND.value(), "User not found " + e.getMessage()
            );
        } catch (IllegalArgumentException e) {
            return new ResponseData<>(
                    HttpStatus.BAD_REQUEST.value(), "Invalid data " + e.getMessage()
            );
        } catch (Exception e) {
            return new ResponseData<>(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(), "Somthing went wrong" + e.getMessage()
            );
        }
    }

    @PatchMapping("/{userId}")
    public ResponseData<Void> changeStatus(
            @PathVariable @Min(1) int userId,
            @RequestParam UserStatus status
    ) {
        try {
            log.info("Request change status userId={}, status={}", userId, status);
            userService.changeStatus(userId, status);
            return new ResponseData<>(
                    HttpStatus.OK.value(),
                    "User status changed"
            );
        } catch (ResourceNotFoundException e) {
            log.warn("User not found userId={}", userId);
            return new ResponseError(
                    HttpStatus.NOT_FOUND.value(),
                    e.getMessage()
            );
        } catch (Exception e) {
            log.error("Change status failed userId={}", userId, e);
            return new ResponseError(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Change status failed"
            );
        }
    }

    @Operation(summary = "Delete user", description = "API delete user")
    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseData<Void> deleteUser(@PathVariable @Min(1) int userId) {
        try {
            userService.deleteUser(userId);
            log.info("Delete user success userId={}", userId);
            return new ResponseData<>(
                    HttpStatus.NO_CONTENT.value(),
                    "User deleted"
            );
        } catch (ResourceNotFoundException e) {
            log.warn("User not found userId={}", userId);
            return new ResponseError(
                    HttpStatus.NOT_FOUND.value(),
                    e.getMessage()
            );
        } catch (Exception e) {
            log.error("Delete user failed userId={}", userId, e);
            return new ResponseError(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Delete user failed"
            );
        }
    }

    @Operation(summary = "Get detail user", description = "API get detail user")
    @GetMapping("/{userId}")
    public ResponseData<UserDetailResponse> getUser(@PathVariable @Min(1) int userId) {
        log.info("Get userId = {}", userId);
        try {
            return new ResponseData<>(
                    HttpStatus.OK.value(),
                    "User",
                    userService.getUser(userId)
            );
        } catch (ResourceNotFoundException e) {
            log.error("errorMessage = {}", e.getMessage(), e.getCause());
            return new ResponseError(
                    HttpStatus.BAD_REQUEST.value(), e.getMessage()
            );
        }

    }

    @Operation(summary = "Get list user", description = "API get list user")
    @GetMapping("/list")
    public ResponseData<?> getAllUser(@RequestParam(defaultValue = "0", required = false) int pageNo,
                                      @Min(10) @RequestParam(defaultValue = "20", required = false) int pageSize,
                                      @RequestParam(required = false) String sortBy
    ) {
        log.info("Request get all of users");
        return new ResponseData<>(HttpStatus.OK.value(), "user", userService.getAllUser(pageNo, pageSize, sortBy));
    }

    @Operation(summary = "Get list user multiple", description = "API get list user multiple")
    @GetMapping("/list-multiple")
    public ResponseData<?> getAllUserWithSortByMultipleColumns(@RequestParam(defaultValue = "0", required = false) int pageNo,
                                                               @Min(10) @RequestParam(defaultValue = "20", required = false) int pageSize,
                                                               @RequestParam(required = false) String... sorts
    ) {

        log.info("Request get all of users with sort by multiple columns");
        return new ResponseData<>(HttpStatus.OK.value(), "user", userService.getAllUserWithSortByMultipleColumns(pageNo, pageSize, sorts));
    }

    @Operation(summary = "Get list user search", description = "API get list user search and columns")
    @GetMapping("/list-search")
    public ResponseData<?> getAllUserWithSortByColumnsAndSearch(@RequestParam(defaultValue = "0", required = false) int pageNo,
                                                                @Min(10) @RequestParam(defaultValue = "20", required = false) int pageSize,
                                                                @RequestParam(required = false) String search,
                                                                @RequestParam(required = false) String sortBy) {
        log.info("Request get all of users with sort by multiple columns and search");
        return new ResponseData<>(HttpStatus.OK.value(), "user", userService.getAllUserWithSortByColumnsAndSearch(pageNo, pageSize, search, sortBy));
    }


    @Operation(summary = "Get list user criteria query", description = "Get list user criteria query")
    @GetMapping("/advance-search-by-criteria")
    public ResponseData<?> advanceSearchByCriteria(@RequestParam(defaultValue = "0", required = false) int pageNo,
                                         @Min(10) @RequestParam(defaultValue = "20", required = false) int pageSize,
                                         @RequestParam(required = false) String sortBy,
                                         @RequestParam(required = false) String... search
    ) {
        log.info("Request get all of users with criteria and sort by and paging");
        return new ResponseData<>(HttpStatus.OK.value(), "user", userService.getAdvanceSearchByCriteria(pageNo, pageSize, sortBy, search));
    }
}
