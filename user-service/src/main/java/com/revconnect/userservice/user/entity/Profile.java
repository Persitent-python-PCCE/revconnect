package com.revconnect.userservice.user.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "profiles")
public class Profile {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @OneToOne @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;
    @Column(name = "full_name") private String fullName;
    private String bio;
    @Column(name = "profile_picture") private String profilePicture;
    @Column(nullable = false) private String privacy;
    private String location;
    private String website;

    public Profile() {}
    public Long getId(){return id;} public void setId(Long id){this.id=id;}
    public User getUser(){return user;} public void setUser(User user){this.user=user;}
    public String getFullName(){return fullName;} public void setFullName(String v){fullName=v;}
    public String getBio(){return bio;} public void setBio(String v){bio=v;}
    public String getProfilePicture(){return profilePicture;} public void setProfilePicture(String v){profilePicture=v;}
    public String getPrivacy(){return privacy;} public void setPrivacy(String v){privacy=v;}
    public String getLocation(){return location;} public void setLocation(String v){location=v;}
    public String getWebsite(){return website;} public void setWebsite(String v){website=v;}
}
