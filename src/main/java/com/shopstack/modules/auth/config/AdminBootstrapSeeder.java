package com.shopstack.modules.auth.config;

import com.shopstack.common.enums.AccountStatus;
import com.shopstack.common.enums.Gender;
import com.shopstack.modules.user.entity.Role;
import com.shopstack.modules.user.entity.User;
import com.shopstack.modules.user.repository.RoleRepository;
import com.shopstack.modules.user.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Order(2)
public class AdminBootstrapSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminBootstrapSeeder.class);

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${shopstack.admin.default-email:admin@shopstack.com}")
    private String defaultAdminEmail;

    @Value("${shopstack.admin.default-password:Admin@123}")
    private String defaultAdminPassword;

    @Value("${shopstack.admin.default-phone:9999999999}")
    private Long defaultAdminPhone;

    public AdminBootstrapSeeder(UserRepository userRepository,
                                 RoleRepository roleRepository,
                                 PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {

        boolean adminExists = userRepository.existsByRole_Name("ADMIN");

        if (adminExists) {
            return;
        }

        Role adminRole = roleRepository.findByName("ADMIN")
                .orElseThrow(() -> new IllegalStateException("ADMIN role not seeded yet"));

        User admin = User.builder()
                .firstName("Super")
                .lastName("Admin")
                .name("Super Admin")
                .email(defaultAdminEmail)
                .password(passwordEncoder.encode(defaultAdminPassword))
                .phone(defaultAdminPhone)
                .gender(Gender.OTHER)
                .status(AccountStatus.ACTIVE)
                .enabled(true)
                .emailVerified(true)
                .phoneVerified(false)
                .role(adminRole)
                .build();

        userRepository.save(admin);

        log.warn("=================================================================");
        log.warn(" Default ADMIN account created: {} / (password from configuration)", defaultAdminEmail);
        log.warn(" Please log in and change this password immediately.");
        log.warn("=================================================================");
    }
}
