package com.revconnect.interaction.controller;

import com.revconnect.interaction.dto.*;
import com.revconnect.interaction.service.InteractionService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class InteractionController {

    private final InteractionService service;

    public InteractionController(InteractionService service) {
        this.service = service;
    }

    @PostMapping("/posts/{postId}/likes")
    public ResponseEntity<Void> like(@PathVariable Long postId, Authentication a) {
        service.likePost(postId, userId(a));
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/posts/{postId}/likes")
    public ResponseEntity<Void> unlike(@PathVariable Long postId, Authentication a) {
        service.unlikePost(postId, userId(a));
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/posts/{postId}/comments")
    public ResponseEntity<CommentResponse> add(@PathVariable Long postId, @Valid @RequestBody CommentRequest r, Authentication a) {
        return ResponseEntity.ok(service.addComment(postId, userId(a), r));
    }

    @GetMapping("/posts/{postId}/comments")
    public ResponseEntity<List<CommentResponse>> list(@PathVariable Long postId) {
        return ResponseEntity.ok(service.getComments(postId));
    }

    @PutMapping("/comments/{commentId}")
    public ResponseEntity<CommentResponse> update(@PathVariable Long commentId, @Valid @RequestBody CommentRequest r, Authentication a) {
        return ResponseEntity.ok(service.updateComment(commentId, userId(a), r));
    }

    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<Void> delete(@PathVariable Long commentId, Authentication a) {
        service.deleteComment(commentId, userId(a));
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/posts/{postId}/shares")
    public ResponseEntity<Void> share(@PathVariable Long postId, Authentication a) {
        service.sharePost(postId, userId(a));
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/posts/{postId}/reposts")
    public ResponseEntity<Void> repost(@PathVariable Long postId, Authentication a) {
        service.repostPost(postId, userId(a));
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/posts/{postId}/reposts")
    public ResponseEntity<Void> unrepost(@PathVariable Long postId, Authentication a) {
        service.unrepostPost(postId, userId(a));
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/posts/{postId}/engagement")
    public ResponseEntity<EngagementResponse> engagement(@PathVariable Long postId, Authentication a) {
        return ResponseEntity.ok(service.getEngagement(postId, userId(a)));
    }

    private Long userId(Authentication a) {
        return (Long) a.getPrincipal();
    }
}