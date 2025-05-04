package com.Capstone.EventManagementPortal.dto;

public class AuthenticationResponseDTO {
    private String token;

    public AuthenticationResponseDTO(String token) {
        this.token = token;
    }

    public String getToken() { return token; }
}
