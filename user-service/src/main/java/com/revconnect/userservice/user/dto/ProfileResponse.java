package com.revconnect.userservice.user.dto;

public class ProfileResponse {

    private Long userId;
    private String username;
    private String email;
    private String accountType;
    private String fullName;
    private String bio;
    private String profilePicture;
    private String privacy;
    private String location;
    private String website;

    public ProfileResponse(
            Long userId,
            String username,
            String email,
            String accountType,
            String fullName,
            String bio,
            String profilePicture,
            String privacy,
            String location,
            String website
    ) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.accountType = accountType;
        this.fullName = fullName;
        this.bio = bio;
        this.profilePicture = profilePicture;
        this.privacy = privacy;
        this.location = location;
        this.website = website;
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

    public String getFullName() {
        return fullName;
    }

    public String getBio() {
        return bio;
    }

    public String getProfilePicture() {
        return profilePicture;
    }

    public String getPrivacy() {
        return privacy;
    }

    public String getLocation() {
        return location;
    }

    public String getWebsite() {
        return website;
    }
}