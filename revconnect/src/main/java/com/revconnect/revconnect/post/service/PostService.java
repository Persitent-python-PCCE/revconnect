package com.revconnect.revconnect.post.service;

import com.revconnect.revconnect.connection.entity.Follow;
import com.revconnect.revconnect.connection.repository.FollowRepository;
import com.revconnect.revconnect.connection.repository.ConnectionRepository;
import com.revconnect.revconnect.notification.service.NotificationService;
import com.revconnect.revconnect.post.dto.*;
import com.revconnect.revconnect.post.entity.*;
import com.revconnect.revconnect.post.repository.*;
import com.revconnect.revconnect.user.entity.User;
import com.revconnect.revconnect.user.repository.UserRepository;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.*;
import java.nio.file.*;
import java.time.*;
import java.util.*;

@Service
public class PostService {

 private static final Set<String> MIME = Set.of("image/jpeg", "image/png", "image/webp", "image/gif");
 private static final int MAX = 2200;

 private final PostRepository posts;
 private final ProductRepository products;
 private final UserRepository users;
 private final FollowRepository follows;
 private final ConnectionRepository connections;
 private final NotificationService notifications;

 public PostService(PostRepository p, ProductRepository pr, UserRepository u, FollowRepository f, ConnectionRepository c, NotificationService n) {
  posts = p;
  products = pr;
  users = u;
  follows = f;
  connections = c;
  notifications = n;
 }

 @Transactional
 public PostResponse createPost(Long uid, MultipartFile photo, String caption, String hashtags, String visibility, boolean promotional, String ctaText, String ctaUrl, Long productId, String scheduledAt, boolean pinned) {
  User user = requireUser(uid);
  String cap = caption == null ? "" : caption.trim();

  if (cap.length() > MAX) {
   bad("Caption must not exceed " + MAX + " characters");
  }

  String vis = normalizeVisibility(visibility);

  if (productId != null) {
   Product pr = products.findById(productId)
           .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));
   if (!pr.getBusinessId().equals(uid)) {
    bad("You can only tag your own products");
   }
  }

  if (promotional && !"BUSINESS".equalsIgnoreCase(user.getAccountType()) && !"CREATOR".equalsIgnoreCase(user.getAccountType())) {
   bad("Creator or Business account required for promotional posts");
  }

  LocalDateTime scheduled = parseDate(scheduledAt);
  String status = scheduled != null && scheduled.isAfter(LocalDateTime.now()) ? "SCHEDULED" : "PUBLISHED";

  Post p = new Post();
  p.setUserId(uid);
  p.setCaption(cap);
  p.setPhotoUrl(saveImage(photo));
  p.setHashtags(normalizeTags(hashtags, cap));
  p.setVisibility(vis);
  p.setPromotional(promotional);
  p.setCtaText(ctaText);
  p.setCtaUrl(ctaUrl);
  p.setProductId(productId);
  p.setScheduledAt(scheduled);
  p.setPinned(pinned);
  p.setStatus(status);

  p = posts.save(p);

  if ("PUBLISHED".equals(status)) {
   notifyFollowers(p);
  }

  return response(p);
 }

 @Transactional
 public PostResponse updatePost(Long id, Long uid, PostRequest r) {
  Post p = requirePost(id);
  if (!p.getUserId().equals(uid)) {
   forbidden("You are not allowed to edit this post");
  }
  apply(p, r, uid);
  return response(posts.save(p));
 }

 @Transactional
 public void deletePost(Long id, Long uid) {
  Post p = requirePost(id);
  if (!p.getUserId().equals(uid)) {
   forbidden("You are not allowed to delete this post");
  }
  posts.delete(p);
 }

 @Transactional
 public PostResponse getPost(Long id, Long viewer) {
  Post p = requirePost(id);
  if (!canView(p, viewer)) {
   forbidden("This post is private");
  }
  return response(p);
 }

 public List<PostResponse> getMyPosts(Long uid) {
  return posts.findByUserIdOrderByCreatedAtDesc(uid)
          .stream()
          .map(this::response)
          .toList();
 }

 public List<PostResponse> search(String q, Long viewer) {
  String term = (q == null ? "" : q).toLowerCase();
  return posts.findByStatusOrderByCreatedAtDesc("PUBLISHED")
          .stream()
          .filter(p -> canView(p, viewer))
          .filter(p -> (p.getCaption() + " " + Objects.toString(p.getHashtags(), "")).toLowerCase().contains(term))
          .limit(50)
          .map(this::response)
          .toList();
 }

 @Transactional
 public PostResponse pin(Long id, Long uid, boolean value) {
  Post p = requirePost(id);
  if (!p.getUserId().equals(uid)) {
   forbidden("Not your post");
  }
  p.setPinned(value);
  return response(posts.save(p));
 }

 @Scheduled(fixedRate = 60000)
 @Transactional
 public void publishScheduled() {
  List<Post> due = posts.findByStatusAndScheduledAtLessThanEqual("SCHEDULED", LocalDateTime.now());
  for (Post p : due) {
   p.setStatus("PUBLISHED");
   posts.save(p);
   notifyFollowers(p);
  }
 }

 private void apply(Post p, PostRequest r, Long uid) {
  if (r.getCaption() != null) {
   p.setCaption(r.getCaption().trim());
  }
  if (r.getHashtags() != null) {
   p.setHashtags(normalizeTags(r.getHashtags(), p.getCaption()));
  }
  if (r.getVisibility() != null) {
   p.setVisibility(normalizeVisibility(r.getVisibility()));
  }

  p.setPromotional(r.isPromotional());
  p.setCtaText(r.getCtaText());
  p.setCtaUrl(r.getCtaUrl());

  if (r.getProductId() != null) {
   Product pr = products.findById(r.getProductId())
           .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));
   if (!pr.getBusinessId().equals(uid)) {
    forbidden("You can only tag your own products");
   }
   p.setProductId(r.getProductId());
  } else {
   p.setProductId(null);
  }

  p.setPinned(r.isPinned());
  p.setScheduledAt(r.getScheduledAt());

  if (p.getScheduledAt() != null && p.getScheduledAt().isAfter(LocalDateTime.now())) {
   p.setStatus("SCHEDULED");
  } else {
   p.setStatus("PUBLISHED");
  }
 }

 private boolean canView(Post p, Long viewer) {
  if (p.getUserId().equals(viewer) || "PUBLIC".equals(p.getVisibility())) {
   return true;
  }
  if ("PRIVATE".equals(p.getVisibility())) {
   return false;
  }
  if ("FOLLOWERS".equals(p.getVisibility())) {
   return follows.existsByFollowerIdAndFollowingId(viewer, p.getUserId());
  }
  if ("CONNECTIONS".equals(p.getVisibility())) {
   Long a = Math.min(viewer, p.getUserId());
   Long b = Math.max(viewer, p.getUserId());
   return connections.existsByUserOneIdAndUserTwoId(a, b);
  }
  return true;
 }

 private void notifyFollowers(Post p) {
  User actor = users.findById(p.getUserId()).orElse(null);
  if (actor == null) return;

  for (Follow f : follows.findByFollowingId(p.getUserId())) {
   notifications.createNotification(f.getFollowerId(), actor, "created a new post.", "POST_CREATED", p.getId());
  }
 }

 private String normalizeTags(String tags, String caption) {
  Set<String> set = new LinkedHashSet<>();

  if (tags != null) {
   for (String t : tags.split("[,\\s]+")) {
    if (t.isBlank()) continue;
    set.add(t.startsWith("#") ? t.toLowerCase() : "#" + t.toLowerCase());
   }
  }

  if (caption != null) {
   java.util.regex.Matcher m = java.util.regex.Pattern.compile("#[A-Za-z0-9_]+", java.util.regex.Pattern.CASE_INSENSITIVE).matcher(caption);
   while (m.find()) {
    set.add(m.group().toLowerCase());
   }
  }

  return String.join(" ", set);
 }

 private String normalizeVisibility(String v) {
  if (v == null) return "PUBLIC";
  String x = v.toUpperCase();
  return Set.of("PUBLIC", "FOLLOWERS", "CONNECTIONS", "PRIVATE").contains(x) ? x : "PUBLIC";
 }

 private LocalDateTime parseDate(String s) {
  if (s == null || s.isBlank()) return null;
  try {
   return LocalDateTime.parse(s);
  } catch (Exception e) {
   bad("scheduledAt must use yyyy-MM-ddTHH:mm");
   return null;
  }
 }

 private String saveImage(MultipartFile f) {
  if (f == null || f.isEmpty()) return "/uploads/default.jpg";

  String type = f.getContentType();
  if (type == null || !MIME.contains(type.toLowerCase())) {
   bad("Only image files are allowed");
  }

  try {
   Path d = Paths.get("uploads", "posts");
   Files.createDirectories(d);

   String n = f.getOriginalFilename();
   String ext = ".jpg";
   if (n != null && n.contains(".")) {
    String e = n.substring(n.lastIndexOf('.')).toLowerCase();
    if (Set.of(".jpg", ".jpeg", ".png", ".webp", ".gif").contains(e)) {
     ext = e;
    }
   }

   String name = UUID.randomUUID() + ext;
   Files.copy(f.getInputStream(), d.resolve(name), StandardCopyOption.REPLACE_EXISTING);
   return "/uploads/posts/" + name;
  } catch (IOException e) {
   throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to save image");
  }
 }

 private PostResponse response(Post p) {
  return new PostResponse(p, users.findById(p.getUserId()).map(User::getUsername).orElse(null));
 }

 private User requireUser(Long id) {
  return users.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
 }

 private Post requirePost(Long id) {
  return posts.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found"));
 }

 private void bad(String s) {
  throw new ResponseStatusException(HttpStatus.BAD_REQUEST, s);
 }

 private void forbidden(String s) {
  throw new ResponseStatusException(HttpStatus.FORBIDDEN, s);
 }
}