package com.project.library.controller;

import com.project.library.dto.response.CurrentUserResponse;
import com.project.library.dto.response.ResponseData;
import com.project.library.exception.BusinessException;
import com.project.library.model.User;
import com.project.library.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "User Controller", description = "API quản lý thông tin người dùng (profile, settings, v.v.)")
@Validated
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping(value = "/me", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Lấy thông tin profile của người dùng hiện tại",
            description = "Trả về thông tin chi tiết của user đang đăng nhập (bao gồm thông tin student nếu là sinh viên). " +
                    "Yêu cầu Bearer Token hợp lệ trong header Authorization.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Lấy profile thành công",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseData.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Chưa xác thực hoặc token không hợp lệ",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Lỗi hệ thống không mong muốn",
                    content = @Content
            )
    })
    public ResponseEntity<ResponseData<CurrentUserResponse>> getCurrentUser(
            // Nếu sau này cần thêm header hoặc param, có thể khai báo ở đây
            @Parameter(hidden = true) Authentication authentication // ẩn khỏi swagger vì tự động từ SecurityContext
    ) {
        try {
            // Lấy authentication từ SecurityContext
            authentication = SecurityContextHolder.getContext().getAuthentication();

            // Kiểm tra xác thực
            if (authentication == null || !authentication.isAuthenticated()) {
                log.warn("Unauthenticated attempt to access /users/me");
                throw new BusinessException("Vui lòng đăng nhập để truy cập thông tin này");
            }

            // check principal có phải là user không
            Object principal = authentication.getPrincipal();
            if (!(principal instanceof User currentUser)) {
                log.error("Principal không phải instance của User: {}", principal != null ? principal.getClass().getName() : "null");
                throw new BusinessException("Dữ liệu xác thực không hợp lệ");
            }

            // Gọi service để map dữ liệu
            CurrentUserResponse profile = userService.getCurrentUserProfile(currentUser);

            log.info("User {} ({}) đã truy cập profile thành công", currentUser.getUsername(), currentUser.getId());

            return ResponseEntity.ok(
                    new ResponseData<>(
                            HttpStatus.OK.value(),
                            "Lấy thông tin profile thành công",
                            profile
                    )
            );

        } catch (Exception e) {
            log.error("Lỗi không mong muốn khi lấy profile người dùng hiện tại", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseData<>(
                            HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "Đã xảy ra lỗi hệ thống. Vui lòng thử lại sau hoặc liên hệ hỗ trợ."
                    ));
        }
    }
}