package com.shopstack.modules.auth.service;

import com.shopstack.common.enums.Gender;
import com.shopstack.common.enums.AccountStatus;
import com.shopstack.common.exception.BadRequestException;
import com.shopstack.common.exception.ConflictException;
import com.shopstack.modules.auth.dto.AuthResponse;
import com.shopstack.modules.auth.dto.LoginRequest;
import com.shopstack.modules.auth.dto.RegisterRequest;
import com.shopstack.modules.user.entity.Role;
import com.shopstack.modules.user.entity.User;
import com.shopstack.modules.user.repository.RoleRepository;
import com.shopstack.modules.user.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class AuthService {

    // Roles that must never be self-assignable through the public /auth/register endpoint.
    private static final Set<String> PRIVILEGED_ROLES = Set.of("ADMIN", "WAREHOUSE_STAFF");

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RoleRepository roleRepository;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       RoleRepository roleRepository) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.roleRepository = roleRepository;
    }

    /** Public self-registration: only CUSTOMER / VENDOR may be requested. */
    public User register(RegisterRequest request) {
        return register(request, false);
    }

    /** Privileged registration (e.g. an ADMIN provisioning another ADMIN/WAREHOUSE_STAFF account). */
    public User registerPrivileged(RegisterRequest request) {
        return register(request, true);
    }

    private User register(RegisterRequest request, boolean allowPrivilegedRoles) {

        if (request.getEmail() == null || request.getEmail().isBlank()) {
            throw new BadRequestException("Email is required");
        }
        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw new BadRequestException("Password is required");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("An account with this email already exists");
        }

        User user = new User();

        // Placeholder split until RegisterRequest collects firstName/lastName separately
        String[] parts = request.getName() != null ? request.getName().split(" ", 2) : new String[]{"", ""};
        user.setFirstName(parts[0]);
        user.setLastName(parts.length > 1 ? parts[1] : "");

        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setPhone(request.getPhone());
        user.setGender(Gender.OTHER); // placeholder — RegisterRequest doesn't collect gender yet
        user.setStatus(AccountStatus.ACTIVE);
        user.setEnabled(true);
        user.setEmailVerified(false);
        user.setPhoneVerified(false);

        String requestedRole = request.getRole();
        if (requestedRole == null || requestedRole.isBlank()) {
            requestedRole = "CUSTOMER";
        }
        final String roleName = requestedRole.toUpperCase();

        if (!allowPrivilegedRoles && PRIVILEGED_ROLES.contains(roleName)) {
            throw new BadRequestException(
                    "The " + roleName + " role cannot be self-registered. Ask an administrator to create this account.");
        }

        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new BadRequestException("Role not found: " + roleName));
        user.setRole(role);

        return userRepository.save(user);
    }

    public AuthResponse login(LoginRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadRequestException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadRequestException("Invalid email or password");
        }

        String token = jwtService.generateToken(user.getEmail(), user.getRole().getName());

        return new AuthResponse(token);
    }
}
