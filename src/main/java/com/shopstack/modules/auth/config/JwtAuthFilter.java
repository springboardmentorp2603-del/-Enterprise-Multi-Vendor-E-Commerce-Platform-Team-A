package com.shopstack.modules.auth.config;

import com.shopstack.common.enums.AccountStatus;
import com.shopstack.modules.auth.service.JwtService;
import com.shopstack.modules.user.entity.User;
import com.shopstack.modules.user.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    public JwtAuthFilter(JwtService jwtService, UserRepository userRepository) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                     @NonNull HttpServletResponse response,
                                     @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);

            if (jwtService.isTokenValid(token)) {
                String email = jwtService.extractEmail(token);
                String tokenRole = jwtService.extractRole(token);

                // Re-check the account against the DB on every request rather than trusting the
                // token's claims for the full 24h lifetime. This means an admin suspending or
                // disabling a user takes effect immediately instead of waiting for token expiry,
                // and it uses the account's *current* role rather than a possibly-stale one.
                Optional<User> userOpt = userRepository.findByEmail(email);

                if (userOpt.isPresent()) {
                    User user = userOpt.get();
                    boolean enabled = Boolean.TRUE.equals(user.getEnabled());
                    boolean statusOk = user.getStatus() != AccountStatus.BLOCKED
                            && user.getStatus() != AccountStatus.INACTIVE;
                    String currentRole = user.getRole() != null ? user.getRole().getName() : tokenRole;

                    if (enabled && statusOk) {
                        var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + currentRole));

                        var authentication =
                                new UsernamePasswordAuthenticationToken(email, null, authorities);

                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    }
                    // else: leave SecurityContext unauthenticated -> request falls through as
                    // anonymous and gets rejected by the authorization rules downstream.
                }
            }
        }

        filterChain.doFilter(request, response);
    }
}