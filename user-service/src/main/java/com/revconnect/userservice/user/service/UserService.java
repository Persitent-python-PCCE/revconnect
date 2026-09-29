package com.revconnect.userservice.user.service;

import com.revconnect.userservice.user.dto.ProfileResponse;
import com.revconnect.userservice.user.dto.UpdateProfileRequest;
import com.revconnect.userservice.user.entity.Profile;
import com.revconnect.userservice.user.entity.User;
import com.revconnect.userservice.user.repository.ProfileRepository;
import com.revconnect.userservice.user.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;

    public UserService(UserRepository userRepository, ProfileRepository profileRepository) {
        this.userRepository = userRepository;
        this.profileRepository = profileRepository;
    }

    public ProfileResponse getMyProfile(Long userId) {
        return toResponse(requireUser(userId), requireProfile(userId));
    }

    public ProfileResponse updateMyProfile(Long userId, UpdateProfileRequest r) {
        User u = requireUser(userId);
        Profile p = requireProfile(userId);

        p.setFullName(r.getFullName());
        p.setBio(r.getBio());
        p.setProfilePicture(r.getProfilePicture());
        p.setPrivacy(normalizePrivacy(r.getPrivacy()));
        p.setLocation(r.getLocation());
        p.setWebsite(r.getWebsite());

        return toResponse(u, profileRepository.save(p));
    }

    public ProfileResponse getUserProfile(Long userId) {
        return toResponse(requireUser(userId), requireProfile(userId));
    }

    public List<ProfileResponse> searchUsers(String query, String accountType) {
        String q = query == null ? "" : query.trim();

        return userRepository.findByUsernameContainingIgnoreCase(q).stream()
                .filter(u -> accountType == null || accountType.isBlank() || u.getAccountType().equalsIgnoreCase(accountType))
                .map(u -> toResponse(u, requireProfile(u.getId())))
                .toList();
    }

    private String normalizePrivacy(String v) {
        return "PRIVATE".equalsIgnoreCase(v) ? "PRIVATE" : "PUBLIC";
    }

    private User requireUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    private Profile requireProfile(Long id) {
        return profileRepository.findByUserId(id)
                .orElseThrow(() -> new RuntimeException("Profile not found"));
    }

    private ProfileResponse toResponse(User u, Profile p) {
        return new ProfileResponse(
                u.getId(),
                u.getUsername(),
                u.getEmail(),
                u.getAccountType(),
                p.getFullName(),
                p.getBio(),
                p.getProfilePicture(),
                p.getPrivacy(),
                p.getLocation(),
                p.getWebsite()
        );
    }
}