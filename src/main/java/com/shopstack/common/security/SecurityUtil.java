package com.shopstack.common.security;

import com.shopstack.common.exception.ForbiddenException;
import com.shopstack.common.exception.ResourceNotFoundException;
import com.shopstack.modules.user.entity.User;
import com.shopstack.modules.user.repository.UserRepository;
import com.shopstack.modules.vendor.entity.Vendor;
import com.shopstack.modules.vendor.repository.VendorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Resolves the currently authenticated principal.
 * The JwtAuthFilter sets the Authentication "name" to the user's email,
 * so we look the User (and, where relevant, the Vendor profile) up from there.
 */
@Component
@RequiredArgsConstructor
public class SecurityUtil {

    private final UserRepository userRepository;
    private final VendorRepository vendorRepository;

    public String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new ForbiddenException("No authenticated user found in the current request");
        }
        return authentication.getName();
    }

    public User getCurrentUser() {
        String email = getCurrentUserEmail();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user not found: " + email));
    }

    /**
     * Resolves the Vendor profile owned by the currently authenticated user.
     * Throws ResourceNotFoundException if the logged-in VENDOR has not completed vendor onboarding yet.
     */
    public Vendor getCurrentVendor() {
        User user = getCurrentUser();
        return vendorRepository.findByUser_Id(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No vendor profile found for the current user. Please complete vendor registration first."));
    }

    public boolean isCurrentUserAdmin() {
        return SecurityContextHolder.getContext().getAuthentication().getAuthorities()
                .stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }
}
