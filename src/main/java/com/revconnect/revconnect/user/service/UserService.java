package com.revconnect.revconnect.user.service;

import com.revconnect.revconnect.user.dto.ProfileResponse;
import com.revconnect.revconnect.user.dto.UpdateProfileRequest;
import com.revconnect.revconnect.user.entity.Profile;
import com.revconnect.revconnect.user.entity.User;
import com.revconnect.revconnect.user.repository.ProfileRepository;
import com.revconnect.revconnect.user.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;

    public UserService(
            UserRepository userRepository,
            ProfileRepository profileRepository) {

        this.userRepository = userRepository;
        this.profileRepository = profileRepository;
    }

    public ProfileResponse getMyProfile(Long userId) {

        User user = userRepository
                .findById(userId)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        Profile profile = profileRepository
                .findByUserId(user.getId())
                .orElseThrow(() ->
                        new RuntimeException("Profile not found"));

        return new ProfileResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getAccountType(),
                profile.getFullName(),
                profile.getBio(),
                profile.getProfilePicture(),
                profile.getPrivacy()
        );
    }

    public ProfileResponse updateMyProfile(
            Long userId,
            UpdateProfileRequest request) {

        User user = userRepository
                .findById(userId)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        Profile profile = profileRepository
                .findByUserId(user.getId())
                .orElseThrow(() ->
                        new RuntimeException("Profile not found"));

        profile.setFullName(request.getFullName());
        profile.setBio(request.getBio());
        profile.setProfilePicture(request.getProfilePicture());
        profile.setPrivacy(request.getPrivacy());

        profileRepository.save(profile);

        return new ProfileResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getAccountType(),
                profile.getFullName(),
                profile.getBio(),
                profile.getProfilePicture(),
                profile.getPrivacy()
        );
    }
}