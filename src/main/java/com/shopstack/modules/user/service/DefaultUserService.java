package com.shopstack.modules.user.service;

import com.shopstack.common.enums.AccountStatus;
import com.shopstack.common.exception.BadRequestException;
import com.shopstack.common.exception.ResourceNotFoundException;
import com.shopstack.modules.auth.dto.RegisterRequest;
import com.shopstack.modules.user.dto.UserResponse;
import com.shopstack.modules.user.entity.Role;
import com.shopstack.modules.user.entity.User;
import com.shopstack.modules.user.mapper.UserMapper;
import com.shopstack.modules.user.repository.RoleRepository;
import com.shopstack.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DefaultUserService implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserResponse register(RegisterRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email already exists.");
        }

        if (userRepository.existsByPhone(request.getPhone())) {
            throw new BadRequestException("Phone number already exists.");
        }

        Role customerRole = roleRepository.findByName("CUSTOMER")
                .orElseThrow(() -> new ResourceNotFoundException("Default CUSTOMER role not found."));

        User user = userMapper.toEntity(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setStatus(AccountStatus.ACTIVE);
        user.setRole(customerRole);

        return userMapper.toResponse(userRepository.save(user));
    }

    @Override
    public UserResponse getUserById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found."));

        return userMapper.toResponse(user);
    }

    @Override
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(userMapper::toResponse)
                .toList();
    }

    @Override
    public void deleteUser(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found."));

        userRepository.delete(user);
    }
}