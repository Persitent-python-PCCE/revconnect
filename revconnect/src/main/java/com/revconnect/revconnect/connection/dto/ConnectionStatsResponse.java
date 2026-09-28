package com.revconnect.revconnect.connection.dto;

public class ConnectionStatsResponse {
    private long connectionCount;
    private long followerCount;
    private long followingCount;
    private long pendingReceivedCount;
    private long pendingSentCount;

    public ConnectionStatsResponse() {
    }

    public ConnectionStatsResponse(long connectionCount, long followerCount, long followingCount, long pendingReceivedCount, long pendingSentCount) {
        this.connectionCount = connectionCount;
        this.followerCount = followerCount;
        this.followingCount = followingCount;
        this.pendingReceivedCount = pendingReceivedCount;
        this.pendingSentCount = pendingSentCount;
    }

    public long getConnectionCount() {
        return connectionCount;
    }

    public void setConnectionCount(long connectionCount) {
        this.connectionCount = connectionCount;
    }

    public long getFollowerCount() {
        return followerCount;
    }

    public void setFollowerCount(long followerCount) {
        this.followerCount = followerCount;
    }

    public long getFollowingCount() {
        return followingCount;
    }

    public void setFollowingCount(long followingCount) {
        this.followingCount = followingCount;
    }

    public long getPendingReceivedCount() {
        return pendingReceivedCount;
    }

    public void setPendingReceivedCount(long pendingReceivedCount) {
        this.pendingReceivedCount = pendingReceivedCount;
    }

    public long getPendingSentCount() {
        return pendingSentCount;
    }

    public void setPendingSentCount(long pendingSentCount) {
        this.pendingSentCount = pendingSentCount;
    }
}
