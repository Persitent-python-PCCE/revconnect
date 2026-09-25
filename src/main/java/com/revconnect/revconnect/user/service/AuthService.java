package com.revconnect.revconnect.user.service;

import com.revconnect.revconnect.user.dto.LoginRequest;
import com.revconnect.revconnect.user.dto.LoginResponse;
import com.revconnect.revconnect.user.dto.RegisterRequest;
import com.revconnect.revconnect.user.entity.Profile;
import com.revconnect.revconnect.user.entity.User;
import com.revconnect.revconnect.user.repository.ProfileRepository;
import com.revconnect.revconnect.user.repository.UserRepository;
import com.revconnect.revconnect.user.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, ProfileRepository profileRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.profileRepository = profileRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public User register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setAccountType(request.getAccountType());
        User savedUser = userRepository.save(user);
        Profile profile = new Profile();
        profile.setUser(savedUser);
        profile.setPrivacy("PUBLIC");
        profileRepository.save(profile);
        return savedUser;
    }

    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid email or password"));
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid email or password");
        }
        String token = jwtService.generateToken(user.getId(), user.getEmail(), user.getAccountType());
        return new LoginResponse(token, user.getId(), user.getUsername(), user.getEmail(), user.getAccountType());
    }
}