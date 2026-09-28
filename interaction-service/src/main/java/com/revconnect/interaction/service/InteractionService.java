package com.revconnect.interaction.service;

import com.revconnect.interaction.dto.*;
import com.revconnect.interaction.entity.*;
import com.revconnect.interaction.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;

@Service
public class InteractionService {
    private static final int MAX_COMMENT_LENGTH = 2000;
    private final PostLikeRepository likes;
    private final CommentRepository comments;
    private final ShareRepository shares;
    private final RepostRepository reposts;

    public InteractionService(PostLikeRepository likes, CommentRepository comments,
                              ShareRepository shares, RepostRepository reposts) {
        this.likes = likes; this.comments = comments; this.shares = shares; this.reposts = reposts;
    }

    @Transactional
    public void likePost(Long postId, Long userId) {
        if (!likes.existsByPostIdAndUserId(postId, userId)) {
            PostLike like = new PostLike(); like.setPostId(postId); like.setUserId(userId); likes.save(like);
        }
    }

    @Transactional public void unlikePost(Long postId, Long userId) { likes.deleteByPostIdAndUserId(postId, userId); }

    @Transactional
    public CommentResponse addComment(Long postId, Long userId, CommentRequest request) {
        Comment comment = new Comment(); comment.setPostId(postId); comment.setUserId(userId); comment.setContent(normalize(request.getContent()));
        return toResponse(comments.save(comment));
    }

    @Transactional(readOnly = true)
    public List<CommentResponse> getComments(Long postId) {
        return comments.findByPostIdOrderByCreatedAtAsc(postId).stream().map(this::toResponse).toList();
    }

    @Transactional
    public CommentResponse updateComment(Long commentId, Long userId, CommentRequest request) {
        Comment comment = requireComment(commentId); verifyOwner(comment, userId); comment.setContent(normalize(request.getContent()));
        return toResponse(comments.save(comment));
    }

    @Transactional
    public void deleteComment(Long commentId, Long userId) {
        Comment comment = requireComment(commentId); verifyOwner(comment, userId); comments.delete(comment);
    }

    @Transactional public void sharePost(Long postId, Long userId) { Share share = new Share(); share.setPostId(postId); share.setUserId(userId); shares.save(share); }

    @Transactional
    public void repostPost(Long postId, Long userId) {
        if (!reposts.existsByPostIdAndUserId(postId, userId)) {
            Repost repost = new Repost(); repost.setPostId(postId); repost.setUserId(userId); reposts.save(repost);
        }
    }

    @Transactional public void unrepostPost(Long postId, Long userId) { reposts.deleteByPostIdAndUserId(postId, userId); }

    @Transactional(readOnly = true)
    public EngagementResponse getEngagement(Long postId, Long userId) {
        return new EngagementResponse(postId, likes.countByPostId(postId), comments.countByPostId(postId), shares.countByPostId(postId), reposts.countByPostId(postId), likes.existsByPostIdAndUserId(postId, userId), reposts.existsByPostIdAndUserId(postId, userId));
    }

    private String normalize(String content) {
        String normalized = content == null ? "" : content.trim();
        if (normalized.isEmpty()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Comment cannot be blank");
        if (normalized.length() > MAX_COMMENT_LENGTH) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Comment must not exceed " + MAX_COMMENT_LENGTH + " characters");
        return normalized;
    }
    private Comment requireComment(Long id) { return comments.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Comment not found")); }
    private void verifyOwner(Comment comment, Long userId) { if (!comment.getUserId().equals(userId)) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not allowed to modify this comment"); }
    private CommentResponse toResponse(Comment comment) { return new CommentResponse(comment, null); }
}
