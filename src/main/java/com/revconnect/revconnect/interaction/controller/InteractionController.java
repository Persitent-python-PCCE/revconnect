package com.revconnect.revconnect.interaction.controller;

import com.revconnect.revconnect.interaction.dto.CommentRequest;
import com.revconnect.revconnect.interaction.dto.CommentResponse;
import com.revconnect.revconnect.interaction.dto.EngagementResponse;
import com.revconnect.revconnect.interaction.service.InteractionService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class InteractionController {

    private final InteractionService interactionService;

    public InteractionController(InteractionService interactionService) {
        this.interactionService = interactionService;
    }

    @PostMapping("/posts/{postId}/likes")
    public ResponseEntity<Void> likePost(@PathVariable Long postId,
                                         Authentication authentication) {
        interactionService.likePost(postId, currentUserId(authentication));
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/posts/{postId}/likes")
    public ResponseEntity<Void> unlikePost(@PathVariable Long postId,
                                           Authentication authentication) {
        interactionService.unlikePost(postId, currentUserId(authentication));
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/posts/{postId}/comments")
    public ResponseEntity<CommentResponse> addComment(
            @PathVariable Long postId,
            @Valid @RequestBody CommentRequest request,
            Authentication authentication) {
        return ResponseEntity.ok(interactionService.addComment(
                postId, currentUserId(authentication), request));
    }

    @GetMapping("/posts/{postId}/comments")
    public ResponseEntity<List<CommentResponse>> getComments(@PathVariable Long postId) {
        return ResponseEntity.ok(interactionService.getComments(postId));
    }

    @PutMapping("/comments/{commentId}")
    public ResponseEntity<CommentResponse> updateComment(
            @PathVariable Long commentId,
            @Valid @RequestBody CommentRequest request,
            Authentication authentication) {
        return ResponseEntity.ok(interactionService.updateComment(
                commentId, currentUserId(authentication), request));
    }

    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<Void> deleteComment(@PathVariable Long commentId,
                                              Authentication authentication) {
        interactionService.deleteComment(commentId, currentUserId(authentication));
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/posts/{postId}/shares")
    public ResponseEntity<Void> sharePost(@PathVariable Long postId,
                                          Authentication authentication) {
        interactionService.sharePost(postId, currentUserId(authentication));
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/posts/{postId}/reposts")
    public ResponseEntity<Void> repostPost(@PathVariable Long postId,
                                           Authentication authentication) {
        interactionService.repostPost(postId, currentUserId(authentication));
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/posts/{postId}/reposts")
    public ResponseEntity<Void> unrepostPost(@PathVariable Long postId,
                                             Authentication authentication) {
        interactionService.unrepostPost(postId, currentUserId(authentication));
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/posts/{postId}/engagement")
    public ResponseEntity<EngagementResponse> getEngagement(
            @PathVariable Long postId,
            Authentication authentication) {
        return ResponseEntity.ok(interactionService.getEngagement(
                postId, currentUserId(authentication)));
    }

    private Long currentUserId(Authentication authentication) {
        return (Long) authentication.getPrincipal();
    }
}
