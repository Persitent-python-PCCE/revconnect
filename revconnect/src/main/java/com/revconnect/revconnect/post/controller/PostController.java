package com.revconnect.revconnect.post.controller;

import com.revconnect.revconnect.post.dto.*;
import com.revconnect.revconnect.post.service.PostService;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/posts")
public class PostController {

 private final PostService postService;

 public PostController(PostService postService) {
  this.postService = postService;
 }

 private Long getUserId(Authentication authentication) {
  return (Long) authentication.getPrincipal();
 }

 @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
 public ResponseEntity<PostResponse> create(
         @RequestParam(value = "photo", required = false) MultipartFile photo,
         @RequestParam(value = "caption", required = false) String caption,
         @RequestParam(value = "hashtags", required = false) String hashtags,
         @RequestParam(value = "visibility", defaultValue = "PUBLIC") String visibility,
         @RequestParam(value = "promotional", defaultValue = "false") boolean promotional,
         @RequestParam(value = "ctaText", required = false) String ctaText,
         @RequestParam(value = "ctaUrl", required = false) String ctaUrl,
         @RequestParam(value = "productId", required = false) Long productId,
         @RequestParam(value = "scheduledAt", required = false) String scheduledAt,
         @RequestParam(value = "pinned", defaultValue = "false") boolean pinned,
         Authentication authentication) {

  return ResponseEntity.status(HttpStatus.CREATED).body(
          postService.createPost(
                  getUserId(authentication),
                  photo,
                  caption,
                  hashtags,
                  visibility,
                  promotional,
                  ctaText,
                  ctaUrl,
                  productId,
                  scheduledAt,
                  pinned
          )
  );
 }

 @GetMapping("/my")
 public List<PostResponse> mine(Authentication authentication) {
  return postService.getMyPosts(getUserId(authentication));
 }

 @GetMapping("/search")
 public List<PostResponse> search(
         @RequestParam String q,
         Authentication authentication) {
  return postService.search(q, getUserId(authentication));
 }

 @GetMapping("/{postId}")
 public PostResponse get(
         @PathVariable Long postId,
         Authentication authentication) {
  return postService.getPost(postId, getUserId(authentication));
 }

 @PutMapping("/{postId}")
 public PostResponse update(
         @PathVariable Long postId,
         @RequestBody PostRequest request,
         Authentication authentication) {
  return postService.updatePost(postId, getUserId(authentication), request);
 }

 @DeleteMapping("/{postId}")
 public ResponseEntity<Void> delete(
         @PathVariable Long postId,
         Authentication authentication) {
  postService.deletePost(postId, getUserId(authentication));
  return ResponseEntity.noContent().build();
 }

 @PutMapping("/{postId}/pin")
 public PostResponse pin(
         @PathVariable Long postId,
         @RequestParam boolean value,
         Authentication authentication) {
  return postService.pin(postId, getUserId(authentication), value);
 }
}