package com.revconnect.revconnect.user.dto;

public class RegisterResponse {

    private Long userId;
    private String username;
    private String email;
    private String accountType;

    public RegisterResponse(
            Long userId,
            String username,
            String email,
            String accountType) {

        this.userId = userId;
        this.username = username;
        this.email = email;
        this.accountType = accountType;
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