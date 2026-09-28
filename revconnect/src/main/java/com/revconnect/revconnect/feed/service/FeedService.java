package com.revconnect.revconnect.feed.service;

import com.revconnect.revconnect.connection.entity.Connection;
import com.revconnect.revconnect.connection.repository.ConnectionRepository;
import com.revconnect.revconnect.connection.repository.FollowRepository;
import com.revconnect.revconnect.feed.dto.FeedResponse;
import com.revconnect.revconnect.interaction.repository.*;
import com.revconnect.revconnect.post.dto.PostResponse;
import com.revconnect.revconnect.post.entity.Post;
import com.revconnect.revconnect.post.repository.PostRepository;
import com.revconnect.revconnect.user.entity.User;
import com.revconnect.revconnect.user.repository.UserRepository;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class FeedService {

 private final PostRepository posts;
 private final UserRepository users;
 private final FollowRepository follows;
 private final ConnectionRepository connections;
 private final PostLikeRepository likes;
 private final CommentRepository comments;
 private final ShareRepository shares;
 private final RepostRepository reposts;

 public FeedService(PostRepository posts,
                    UserRepository users,
                    FollowRepository follows,
                    ConnectionRepository connections,
                    PostLikeRepository likes,
                    CommentRepository comments,
                    ShareRepository shares,
                    RepostRepository reposts) {
  this.posts = posts;
  this.users = users;
  this.follows = follows;
  this.connections = connections;
  this.likes = likes;
  this.comments = comments;
  this.shares = shares;
  this.reposts = reposts;
 }

 public FeedResponse getFeed(Long uid, String type, String hashtag, int page, int size) {
  String mode = type == null ? "latest" : type.toLowerCase();

  Set<Long> allowed = new HashSet<>();
  allowed.add(uid);

  if (mode.equals("following") || mode.equals("personalized")) {
   follows.findByFollowerId(uid, Pageable.unpaged())
           .forEach(f -> allowed.add(f.getFollowingId()));
  }

  if (mode.equals("connections") || mode.equals("personalized")) {
   connections.findAllByUserId(uid, Pageable.unpaged())
           .forEach(c -> allowed.add(c.getUserOneId().equals(uid) ? c.getUserTwoId() : c.getUserOneId()));
  }

  List<Post> all = posts.findByStatusOrderByCreatedAtDesc("PUBLISHED");

  List<Post> filtered = all.stream()
          .filter(p -> canView(p, uid))
          .filter(p -> !mode.equals("following") || allowed.contains(p.getUserId()))
          .filter(p -> !mode.equals("connections") || allowed.contains(p.getUserId()))
          .filter(p -> !mode.equals("personalized") || allowed.contains(p.getUserId()) || "PUBLIC".equals(p.getVisibility()))
          .filter(p -> {
           if (hashtag == null || hashtag.isBlank()) return true;

           String formattedHashtag = hashtag.toLowerCase().startsWith("#") ? hashtag.toLowerCase() : "#" + hashtag.toLowerCase();
           String postHashtags = Objects.toString(p.getHashtags(), "").toLowerCase();

           return postHashtags.contains(formattedHashtag);
          })
          .toList();

  if (mode.equals("trending")) {
   filtered = filtered.stream()
           .sorted(Comparator.comparingLong(this::score)
                   .reversed()
                   .thenComparing(Post::getCreatedAt, Comparator.reverseOrder()))
           .toList();
  }

  int from = Math.min(page * size, filtered.size());
  int to = Math.min(from + size, filtered.size());

  List<PostResponse> out = filtered.subList(from, to).stream()
          .map(p -> new PostResponse(
                  p,
                  users.findById(p.getUserId()).map(User::getUsername).orElse(null)
          ))
          .toList();

  int total = (int) Math.ceil(filtered.size() / (double) size);
  boolean isLast = to >= filtered.size();

  return new FeedResponse(out, page, size, total, filtered.size(), isLast);
 }

 private long score(Post p) {
  return (likes.countByPostId(p.getId()) * 3L) +
          (comments.countByPostId(p.getId()) * 2L) +
          (shares.countByPostId(p.getId())) +
          (reposts.countByPostId(p.getId()) * 2L);
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
}