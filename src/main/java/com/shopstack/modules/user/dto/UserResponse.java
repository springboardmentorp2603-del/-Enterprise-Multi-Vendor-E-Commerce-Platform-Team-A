package com.shopstack.modules.user.dto;

import com.shopstack.common.enums.AccountStatus;
import com.shopstack.common.enums.Gender;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class UserResponse {
    private UUID id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private Gender gender;
    private AccountStatus status;
    private Boolean enabled;
    private Boolean emailVerified;
    private Boolean phoneVerified;
    private String profileImage;
    private String role;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}