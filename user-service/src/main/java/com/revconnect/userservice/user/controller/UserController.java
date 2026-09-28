package com.revconnect.userservice.user.controller;

import com.revconnect.userservice.user.dto.ProfileResponse;
import com.revconnect.userservice.user.dto.UpdateProfileRequest;
import com.revconnect.userservice.user.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController @RequestMapping("/api/user")
public class UserController {
    private final UserService userService;
    public UserController(UserService userService){this.userService=userService;}
    private Long id(Authentication a){return (Long)a.getPrincipal();}
    @GetMapping("/me") public ResponseEntity<ProfileResponse> me(Authentication a){return ResponseEntity.ok(userService.getMyProfile(id(a)));}
    @PutMapping("/profile") public ResponseEntity<ProfileResponse> update(Authentication a,@RequestBody UpdateProfileRequest r){return ResponseEntity.ok(userService.updateMyProfile(id(a),r));}
    @GetMapping("/creator-test") @PreAuthorize("hasRole('CREATOR')") public ResponseEntity<String> creatorTest(){return ResponseEntity.ok("Creator-only API accessed successfully!");}
    @GetMapping("/{id}") public ResponseEntity<ProfileResponse> get(@PathVariable Long id){return ResponseEntity.ok(userService.getUserProfile(id));}
    @GetMapping("/search") public ResponseEntity<List<ProfileResponse>> search(@RequestParam String q,@RequestParam(required=false) String type){return ResponseEntity.ok(userService.searchUsers(q,type));}
}
