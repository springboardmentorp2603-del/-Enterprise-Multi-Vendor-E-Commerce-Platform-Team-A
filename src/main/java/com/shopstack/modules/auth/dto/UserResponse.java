package com.shopstack.modules.auth.dto;

import com.shopstack.modules.user.entity.User;

public class UserResponse {
    private String id;
    private String firstName;
    private String lastName;
    private String name;
    private String email;
    private Long phone;
    private String gender;
    private String status;
    private String role;

    public UserResponse(User user) {
        this.id = user.getId().toString();
        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();
        this.name = user.getName();
        this.email = user.getEmail();
        this.phone = user.getPhone();
        this.gender = user.getGender() != null ? user.getGender().name() : null;
        this.status = user.getStatus() != null ? user.getStatus().name() : null;
        this.role = user.getRole() != null ? user.getRole().getName() : null;
    }

    // Getters — required since no Lombok here; add manually or annotate with @Getter
    public String getId() { return id; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public Long getPhone() { return phone; }
    public String getGender() { return gender; }
    public String getStatus() { return status; }
    public String getRole() { return role; }
}