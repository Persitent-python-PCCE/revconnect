package com.revconnect.userservice.user.dto;

public class UpdateProfileRequest {
    private String fullName; private String bio; private String profilePicture; private String privacy; private String location; private String website;
    public UpdateProfileRequest(){}
    public String getFullName(){return fullName;} public void setFullName(String v){fullName=v;}
    public String getBio(){return bio;} public void setBio(String v){bio=v;}
    public String getProfilePicture(){return profilePicture;} public void setProfilePicture(String v){profilePicture=v;}
    public String getPrivacy(){return privacy;} public void setPrivacy(String v){privacy=v;}
    public String getLocation(){return location;} public void setLocation(String v){location=v;}
    public String getWebsite(){return website;} public void setWebsite(String v){website=v;}
}
