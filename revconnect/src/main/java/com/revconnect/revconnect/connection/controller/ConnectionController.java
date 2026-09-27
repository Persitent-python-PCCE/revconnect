package com.revconnect.revconnect.connection.controller;

import com.revconnect.revconnect.connection.dto.ConnectionRequestResponse;
import com.revconnect.revconnect.connection.dto.ConnectionResponse;
import com.revconnect.revconnect.connection.dto.ConnectionStatsResponse;
import com.revconnect.revconnect.connection.dto.FollowResponse;
import com.revconnect.revconnect.connection.dto.RelationshipStatusResponse;
import com.revconnect.revconnect.connection.service.ConnectionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/connections")
public class ConnectionController {

    private final ConnectionService connectionService;

    public ConnectionController(ConnectionService connectionService) {
        this.connectionService = connectionService;
    }

    private Long currentUserId(Authentication authentication) {
        return (Long) authentication.getPrincipal();
    }

    private Pageable createPageable(int page, int size) {
        int normalizedPage = Math.max(0, page);
        int normalizedSize = size < 1 ? 20 : Math.min(size, 50);
        return PageRequest.of(normalizedPage, normalizedSize, Sort.by(Sort.Direction.DESC, "createdAt"));
    }

    // --- CONNECTION REQUESTS ---

    @PostMapping("/requests/{targetUserId}")
    public ResponseEntity<ConnectionRequestResponse> sendRequest(Authentication authentication, @PathVariable Long targetUserId) {
        ConnectionRequestResponse response = connectionService.sendRequest(currentUserId(authentication), targetUserId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/requests/received")
    public ResponseEntity<Page<ConnectionRequestResponse>> getReceivedRequests(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<ConnectionRequestResponse> responses = connectionService.getReceivedRequests(currentUserId(authentication), createPageable(page, size));
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/requests/sent")
    public ResponseEntity<Page<ConnectionRequestResponse>> getSentRequests(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<ConnectionRequestResponse> responses = connectionService.getSentRequests(currentUserId(authentication), createPageable(page, size));
        return ResponseEntity.ok(responses);
    }

    @PutMapping("/requests/{requestId}/accept")
    public ResponseEntity<ConnectionRequestResponse> acceptRequest(Authentication authentication, @PathVariable Long requestId) {
        ConnectionRequestResponse response = connectionService.acceptRequest(currentUserId(authentication), requestId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/requests/{requestId}/reject")
    public ResponseEntity<ConnectionRequestResponse> rejectRequest(Authentication authentication, @PathVariable Long requestId) {
        ConnectionRequestResponse response = connectionService.rejectRequest(currentUserId(authentication), requestId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/requests/{requestId}")
    public ResponseEntity<Void> cancelRequest(Authentication authentication, @PathVariable Long requestId) {
        connectionService.cancelRequest(currentUserId(authentication), requestId);
        return ResponseEntity.noContent().build();
    }

    // --- ACTIVE CONNECTIONS ---

    @GetMapping
    public ResponseEntity<Page<ConnectionResponse>> getConnections(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<ConnectionResponse> responses = connectionService.getConnections(currentUserId(authentication), createPageable(page, size));
        return ResponseEntity.ok(responses);
    }

    @DeleteMapping("/{otherUserId}")
    public ResponseEntity<Void> removeConnection(Authentication authentication, @PathVariable Long otherUserId) {
        connectionService.removeConnection(currentUserId(authentication), otherUserId);
        return ResponseEntity.noContent().build();
    }

    // --- FOLLOW ---

    @PostMapping("/follow/{targetUserId}")
    public ResponseEntity<FollowResponse> followUser(Authentication authentication, @PathVariable Long targetUserId) {
        FollowResponse response = connectionService.followUser(currentUserId(authentication), targetUserId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/follow/{targetUserId}")
    public ResponseEntity<Void> unfollowUser(Authentication authentication, @PathVariable Long targetUserId) {
        connectionService.unfollowUser(currentUserId(authentication), targetUserId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/followers")
    public ResponseEntity<Page<FollowResponse>> getFollowers(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<FollowResponse> responses = connectionService.getFollowers(currentUserId(authentication), createPageable(page, size));
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/following")
    public ResponseEntity<Page<FollowResponse>> getFollowing(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<FollowResponse> responses = connectionService.getFollowing(currentUserId(authentication), createPageable(page, size));
        return ResponseEntity.ok(responses);
    }

    // --- STATUS AND STATS ---

    @GetMapping("/status/{targetUserId}")
    public ResponseEntity<RelationshipStatusResponse> getRelationshipStatus(Authentication authentication, @PathVariable Long targetUserId) {
        RelationshipStatusResponse response = connectionService.getRelationshipStatus(currentUserId(authentication), targetUserId);
        return ResponseEntity.ok(response);
    }

    @GetMapping({"/stats", "/stats/{targetUserId}"})
    public ResponseEntity<ConnectionStatsResponse> getStats(
            Authentication authentication,
            @PathVariable(required = false) Long targetUserId) {
        Long userIdToFetch = targetUserId != null ? targetUserId : currentUserId(authentication);
        ConnectionStatsResponse response = connectionService.getStats(userIdToFetch);
        return ResponseEntity.ok(response);
    }
}
