package com.revconnect.revconnect.analytics;

import com.revconnect.revconnect.connection.repository.FollowRepository;
import com.revconnect.revconnect.interaction.repository.*;
import com.revconnect.revconnect.post.entity.Post;
import com.revconnect.revconnect.post.repository.PostRepository;
import com.revconnect.revconnect.user.entity.Profile;
import com.revconnect.revconnect.user.repository.ProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Pageable;

import java.util.*;

@Service
public class AnalyticsService {

 private final PostRepository posts;
 private final PostLikeRepository likes;
 private final CommentRepository comments;
 private final ShareRepository shares;
 private final RepostRepository reposts;
 private final FollowRepository follows;
 private final ProfileRepository profiles;

 public AnalyticsService(PostRepository posts,
                         PostLikeRepository likes,
                         CommentRepository comments,
                         ShareRepository shares,
                         RepostRepository reposts,
                         FollowRepository follows,
                         ProfileRepository profiles) {
  this.posts = posts;
  this.likes = likes;
  this.comments = comments;
  this.shares = shares;
  this.reposts = reposts;
  this.follows = follows;
  this.profiles = profiles;
 }

 public Map<String, Object> user(Long uid) {
  List<Post> mine = posts.findByUserIdOrderByCreatedAtDesc(uid);

  long like = mine.stream().mapToLong(p -> likes.countByPostId(p.getId())).sum();
  long comment = mine.stream().mapToLong(p -> comments.countByPostId(p.getId())).sum();
  long share = mine.stream().mapToLong(p -> shares.countByPostId(p.getId())).sum();
  long repost = mine.stream().mapToLong(p -> reposts.countByPostId(p.getId())).sum();

  long followers = follows.countByFollowingId(uid);

  Map<String, Long> locations = new LinkedHashMap<>();

  for (var f : follows.findByFollowingId(uid, Pageable.unpaged()).getContent()) {
   Profile p = profiles.findByUserId(f.getFollowerId()).orElse(null);

   if (p != null && p.getLocation() != null && !p.getLocation().isBlank()) {
    locations.merge(p.getLocation(), 1L, Long::sum);
   }
  }

  long engagement = like + comment + share + repost;
  long reach = followers + engagement;

  return Map.of(
          "posts", mine.size(),
          "likes", like,
          "comments", comment,
          "shares", share,
          "reposts", repost,
          "followers", followers,
          "reach", reach,
          "engagement", engagement,
          "audienceLocations", locations
  );
 }
}