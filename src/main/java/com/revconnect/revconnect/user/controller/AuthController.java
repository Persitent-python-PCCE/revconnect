package com.revconnect.revconnect.user.controller;

import com.revconnect.revconnect.user.dto.LoginRequest;
import com.revconnect.revconnect.user.dto.LoginResponse;
import com.revconnect.revconnect.user.dto.RegisterRequest;
import com.revconnect.revconnect.user.entity.User;
import com.revconnect.revconnect.user.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.revconnect.revconnect.user.dto.RegisterResponse;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(
            @Valid @RequestBody RegisterRequest request) {

        User user = authService.register(request);

        RegisterResponse response = new RegisterResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getAccountType()
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request) {

        LoginResponse response = authService.login(request);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/test")
    public ResponseEntity<String> test() {

        return ResponseEntity.ok(
                "JWT authentication successful!"
        );
    }
}