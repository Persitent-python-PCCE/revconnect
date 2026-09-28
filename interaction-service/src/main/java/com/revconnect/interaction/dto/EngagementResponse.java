package com.revconnect.interaction.dto;
public class EngagementResponse {
    private Long postId; private long likeCount, commentCount, shareCount, repostCount; private boolean likedByCurrentUser, repostedByCurrentUser;
    public EngagementResponse(Long postId, long likeCount, long commentCount, long shareCount, long repostCount, boolean liked, boolean reposted) {
        this.postId = postId; this.likeCount = likeCount; this.commentCount = commentCount; this.shareCount = shareCount; this.repostCount = repostCount; likedByCurrentUser = liked; repostedByCurrentUser = reposted;
    }
    public Long getPostId() { return postId; } public long getLikeCount() { return likeCount; } public long getCommentCount() { return commentCount; }
    public long getShareCount() { return shareCount; } public long getRepostCount() { return repostCount; }
    public boolean isLikedByCurrentUser() { return likedByCurrentUser; } public boolean isRepostedByCurrentUser() { return repostedByCurrentUser; }
}
