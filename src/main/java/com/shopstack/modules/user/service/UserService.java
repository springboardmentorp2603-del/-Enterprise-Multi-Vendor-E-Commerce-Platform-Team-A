package com.shopstack.modules.user.service;

import com.shopstack.modules.auth.dto.RegisterRequest;
import com.shopstack.modules.user.dto.UserResponse;

import java.util.List;
import java.util.UUID;

public interface UserService {
    UserResponse register(RegisterRequest request);
    UserResponse getUserById(UUID id);
    List<UserResponse> getAllUsers();
    void deleteUser(UUID id);
}