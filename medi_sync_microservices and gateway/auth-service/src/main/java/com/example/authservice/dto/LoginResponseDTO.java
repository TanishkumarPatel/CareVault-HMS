package com.example.authservice.dto;

public class LoginResponseDTO {
    private final String token;

//    because there is only one variable so we can use constructor instead of setter
    public LoginResponseDTO(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }
}
