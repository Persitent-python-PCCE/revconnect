package com.revconnect.revconnect.post.controller;

import com.revconnect.revconnect.post.dto.PostRequest;
import com.revconnect.revconnect.post.dto.PostResponse;
import com.revconnect.revconnect.post.service.PostService;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/posts")
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    /**
     * Create a new post.
     *
     * Accepts multipart/form-data with:
     * - photo   (required)
     * - caption (optional)
     *
     * User ID comes from the authenticated JWT.
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PostResponse> createPost(
            @RequestParam("photo") MultipartFile photo,
            @RequestParam(value = "caption", required = false) String caption,
            Authentication authentication) {

        Long userId = (Long) authentication.getPrincipal();

        return ResponseEntity.ok(
                postService.createPost(userId, photo, caption)
        );
    }

    /**
     * Get posts created by the logged-in user.
     */
    @GetMapping("/my")
    public ResponseEntity<List<PostResponse>> getMyPosts(
            Authentication authentication) {

        Long userId = (Long) authentication.getPrincipal();

        return ResponseEntity.ok(
                postService.getMyPosts(userId)
        );
    }

    /**
     * Get a single post by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<PostResponse> getPost(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                postService.getPost(id)
        );
    }

    /**
     * Update a post caption.
     */
    @PutMapping("/{id}")
    public ResponseEntity<PostResponse> updatePost(
            @PathVariable Long id,
            @RequestBody PostRequest request,
            Authentication authentication) {

        Long userId = (Long) authentication.getPrincipal();

        return ResponseEntity.ok(
                postService.updatePost(
                        id,
                        userId,
                        request
                )
        );
    }

    /**
     * Delete a post.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(
            @PathVariable Long id,
            Authentication authentication) {

        Long userId = (Long) authentication.getPrincipal();

        postService.deletePost(id, userId);

        return ResponseEntity.noContent().build();
    }
}