package com.shopstack.modules.auth.controller;
import com.shopstack.common.security.SecurityUtil;
import com.shopstack.modules.auth.dto.UserResponse;
import com.shopstack.modules.auth.dto.AuthResponse;
import com.shopstack.modules.auth.dto.LoginRequest;
import com.shopstack.modules.auth.dto.RegisterRequest;
import com.shopstack.modules.auth.service.AuthService;
import com.shopstack.modules.user.entity.User;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private final SecurityUtil securityUtil;

    public AuthController(AuthService authService, SecurityUtil securityUtil) {
        this.authService = authService;
        this.securityUtil = securityUtil;
    }

    @PostMapping("/register")
    public UserResponse register(@Valid @RequestBody RegisterRequest request) {
        User user = authService.register(request);
        return new UserResponse(user);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    // Returns the caller's own account details (incl. id) resolved from the JWT.
    // Useful for the frontend/client to learn its own userId, e.g. for vendor registration.
    @GetMapping("/me")
    public UserResponse me() {
        return new UserResponse(securityUtil.getCurrentUser());
    }
}
