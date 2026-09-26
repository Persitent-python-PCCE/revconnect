package com.revconnect.revconnect.interaction.dto;

public class EngagementResponse {

    private Long postId;
    private long likeCount;
    private long commentCount;
    private long shareCount;
    private long repostCount;
    private boolean likedByCurrentUser;
    private boolean repostedByCurrentUser;

    public EngagementResponse(Long postId, long likeCount, long commentCount,
                              long shareCount, long repostCount,
                              boolean likedByCurrentUser, boolean repostedByCurrentUser) {
        this.postId = postId;
        this.likeCount = likeCount;
        this.commentCount = commentCount;
        this.shareCount = shareCount;
        this.repostCount = repostCount;
        this.likedByCurrentUser = likedByCurrentUser;
        this.repostedByCurrentUser = repostedByCurrentUser;
    }

    public Long getPostId() { return postId; }
    public long getLikeCount() { return likeCount; }
    public long getCommentCount() { return commentCount; }
    public long getShareCount() { return shareCount; }
    public long getRepostCount() { return repostCount; }
    public boolean isLikedByCurrentUser() { return likedByCurrentUser; }
    public boolean isRepostedByCurrentUser() { return repostedByCurrentUser; }
}
