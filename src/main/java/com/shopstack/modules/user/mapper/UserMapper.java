package com.shopstack.modules.user.mapper;

import com.shopstack.common.config.MapStructConfig;
import com.shopstack.modules.auth.dto.RegisterRequest;
import com.shopstack.modules.user.dto.UserResponse;
import com.shopstack.modules.user.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MapStructConfig.class)
public interface UserMapper {
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "enabled", constant = "true")
    @Mapping(target = "emailVerified", constant = "false")
    @Mapping(target = "phoneVerified", constant = "false")
    @Mapping(target = "failedLoginAttempts", constant = "0")
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "profileImage", ignore = true)
    User toEntity(RegisterRequest request);

    @Mapping(source = "role.name", target = "role")
    UserResponse toResponse(User user);
}