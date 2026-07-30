package com.shopstack.modules.user.repository;

import com.shopstack.modules.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID>, JpaSpecificationExecutor<User> {
    Optional<User> findByEmail(String email);
    Optional<User> findByPhone(Long phone);
    boolean existsByEmail(String email);
    boolean existsByPhone(Long phone);
    boolean existsByRole_Name(String roleName);
}