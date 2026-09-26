package com.revconnect.revconnect.connection.dto;

public class RelationshipStatusResponse {
    private Long targetUserId;
    private String targetUsername;
    private String connectionStatus;
    private boolean following;
    private boolean followedBy;

    public RelationshipStatusResponse() {
    }

    public RelationshipStatusResponse(Long targetUserId, String targetUsername, String connectionStatus, boolean following, boolean followedBy) {
        this.targetUserId = targetUserId;
        this.targetUsername = targetUsername;
        this.connectionStatus = connectionStatus;
        this.following = following;
        this.followedBy = followedBy;
    }

    public Long getTargetUserId() {
        return targetUserId;
    }

    public void setTargetUserId(Long targetUserId) {
        this.targetUserId = targetUserId;
    }

    public String getTargetUsername() {
        return targetUsername;
    }

    public void setTargetUsername(String targetUsername) {
        this.targetUsername = targetUsername;
    }

    public String getConnectionStatus() {
        return connectionStatus;
    }

    public void setConnectionStatus(String connectionStatus) {
        this.connectionStatus = connectionStatus;
    }

    public boolean isFollowing() {
        return following;
    }

    public void setFollowing(boolean following) {
        this.following = following;
    }

    public boolean isFollowedBy() {
        return followedBy;
    }

    public void setFollowedBy(boolean followedBy) {
        this.followedBy = followedBy;
    }
}
