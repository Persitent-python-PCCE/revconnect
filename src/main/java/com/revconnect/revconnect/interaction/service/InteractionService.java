package com.revconnect.revconnect.interaction.service;

import com.revconnect.revconnect.interaction.dto.CommentRequest;
import com.revconnect.revconnect.interaction.dto.CommentResponse;
import com.revconnect.revconnect.interaction.dto.EngagementResponse;
import com.revconnect.revconnect.interaction.entity.Comment;
import com.revconnect.revconnect.interaction.entity.PostLike;
import com.revconnect.revconnect.interaction.entity.Repost;
import com.revconnect.revconnect.interaction.entity.Share;
import com.revconnect.revconnect.interaction.repository.CommentRepository;
import com.revconnect.revconnect.interaction.repository.PostLikeRepository;
import com.revconnect.revconnect.interaction.repository.RepostRepository;
import com.revconnect.revconnect.interaction.repository.ShareRepository;
import com.revconnect.revconnect.notification.service.NotificationService;
import com.revconnect.revconnect.post.entity.Post;
import com.revconnect.revconnect.post.repository.PostRepository;
import com.revconnect.revconnect.user.entity.User;
import com.revconnect.revconnect.user.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class InteractionService {

    private static final int MAX_COMMENT_LENGTH = 2000;

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final PostLikeRepository postLikeRepository;
    private final CommentRepository commentRepository;
    private final ShareRepository shareRepository;
    private final RepostRepository repostRepository;
    private final NotificationService notificationService;

    public InteractionService(PostRepository postRepository,
                              UserRepository userRepository,
                              PostLikeRepository postLikeRepository,
                              CommentRepository commentRepository,
                              ShareRepository shareRepository,
                              RepostRepository repostRepository,
                              NotificationService notificationService) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.postLikeRepository = postLikeRepository;
        this.commentRepository = commentRepository;
        this.shareRepository = shareRepository;
        this.repostRepository = repostRepository;
        this.notificationService = notificationService;
    }

    @Transactional
    public void likePost(Long postId, Long userId) {
        Post post = requirePost(postId);
        User user = requireUser(userId);

        if (!postLikeRepository.existsByPostIdAndUserId(postId, userId)) {
            PostLike like = new PostLike();
            like.setPostId(postId);
            like.setUserId(userId);
            postLikeRepository.save(like);
            notifyPostAuthor(post, user, "liked your post.", "LIKE");
        }
    }

    @Transactional
    public void unlikePost(Long postId, Long userId) {
        requirePost(postId);
        postLikeRepository.deleteByPostIdAndUserId(postId, userId);
    }

    @Transactional
    public CommentResponse addComment(Long postId, Long userId, CommentRequest request) {
        Post post = requirePost(postId);
        User user = requireUser(userId);
        String content = normalizeComment(request.getContent());

        Comment comment = new Comment();
        comment.setPostId(postId);
        comment.setUserId(userId);
        comment.setContent(content);

        Comment savedComment = commentRepository.save(comment);
        notifyPostAuthor(post, user, "commented on your post.", "COMMENT");

        return toCommentResponse(savedComment, user.getUsername());
    }

    @Transactional(readOnly = true)
    public List<CommentResponse> getComments(Long postId) {
        requirePost(postId);
        return commentRepository.findByPostIdOrderByCreatedAtAsc(postId)
                .stream()
                .map(comment -> toCommentResponse(comment,
                        userRepository.findById(comment.getUserId())
                                .map(User::getUsername)
                                .orElse(null)))
                .toList();
    }

    @Transactional
    public CommentResponse updateComment(Long commentId, Long userId, CommentRequest request) {
        Comment comment = requireComment(commentId);
        verifyCommentOwner(comment, userId);
        String content = normalizeComment(request.getContent());
        comment.setContent(content);
        Comment updated = commentRepository.save(comment);

        return toCommentResponse(updated,
                userRepository.findById(updated.getUserId())
                        .map(User::getUsername)
                        .orElse(null));
    }

    @Transactional
    public void deleteComment(Long commentId, Long userId) {
        Comment comment = requireComment(commentId);
        verifyCommentOwner(comment, userId);
        commentRepository.delete(comment);
    }

    @Transactional
    public void sharePost(Long postId, Long userId) {
        Post post = requirePost(postId);
        User user = requireUser(userId);

        Share share = new Share();
        share.setPostId(postId);
        share.setUserId(userId);
        shareRepository.save(share);
        notifyPostAuthor(post, user, "shared your post.", "SHARE");
    }

    @Transactional
    public void repostPost(Long postId, Long userId) {
        Post post = requirePost(postId);
        User user = requireUser(userId);

        if (!repostRepository.existsByPostIdAndUserId(postId, userId)) {
            Repost repost = new Repost();
            repost.setPostId(postId);
            repost.setUserId(userId);
            repostRepository.save(repost);
            notifyPostAuthor(post, user, "reposted your post.", "REPOST");
        }
    }

    @Transactional
    public void unrepostPost(Long postId, Long userId) {
        requirePost(postId);
        repostRepository.deleteByPostIdAndUserId(postId, userId);
    }

    @Transactional(readOnly = true)
    public EngagementResponse getEngagement(Long postId, Long userId) {
        requirePost(postId);
        return new EngagementResponse(
                postId,
                postLikeRepository.countByPostId(postId),
                commentRepository.countByPostId(postId),
                shareRepository.countByPostId(postId),
                repostRepository.countByPostId(postId),
                postLikeRepository.existsByPostIdAndUserId(postId, userId),
                repostRepository.existsByPostIdAndUserId(postId, userId)
        );
    }

    private String normalizeComment(String content) {
        String normalized = content == null ? "" : content.trim();
        if (normalized.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Comment cannot be blank");
        }
        if (normalized.length() > MAX_COMMENT_LENGTH) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Comment must not exceed " + MAX_COMMENT_LENGTH + " characters");
        }
        return normalized;
    }

    private void verifyCommentOwner(Comment comment, Long userId) {
        if (!comment.getUserId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "You are not allowed to modify this comment");
        }
    }

    private Comment requireComment(Long commentId) {
        return commentRepository.findById(commentId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Comment not found"));
    }

    private Post requirePost(Long postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found"));
    }

    private void notifyPostAuthor(Post post, User actor, String message, String type) {
        if (!post.getUserId().equals(actor.getId())) {
            notificationService.createNotification(
                    post.getUserId(),
                    actor,
                    actor.getUsername() + " " + message,
                    type,
                    post.getId()
            );
        }
    }

    private User requireUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User not found"));
    }

    private CommentResponse toCommentResponse(Comment comment, String username) {
        return new CommentResponse(comment, username);
    }
}
