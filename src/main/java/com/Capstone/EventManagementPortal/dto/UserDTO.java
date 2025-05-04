package com.Capstone.EventManagementPortal.dto;

import com.Capstone.EventManagementPortal.model.Role;
import com.Capstone.EventManagementPortal.model.User;

public class UserDTO {
    private Long id;
    private String name;
    private String username;
    private String email;
    private Role role;

    public UserDTO(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.email = user.getEmail();
        this.role = user.getRole() != null ? user.getRole() : Role.ATTENDEE;
        this.name = user.getName();
    }

    // Getters
    public Long getId() { return id; }
    public String getName() {
        return name;
    }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public Role getRole() { return role; }

}
