package com.revconnect.revconnect.user.dto;

public class LoginResponse {

    private String token;
    private Long userId;
    private String username;
    private String email;
    private String accountType;

    public LoginResponse(String token,
                         Long userId,
                         String username,
                         String email,
                         String accountType) {

        this.token = token;
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.accountType = accountType;
    }

    public String getToken() {
        return token;
    }

    public Long getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getAccountType() {
        return accountType;
    }
}