package com.shopstack.modules.auth.controller;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OAuthController {
    @GetMapping("/oauth2/user")
    public Object user(Authentication authentication) {
        return authentication;
    }
}