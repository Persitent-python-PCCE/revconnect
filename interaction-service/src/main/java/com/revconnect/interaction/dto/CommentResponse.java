package com.revconnect.interaction.dto;
import com.revconnect.interaction.entity.Comment;
import java.time.LocalDateTime;
public class CommentResponse {
    private Long id, postId, userId; private String username, content; private LocalDateTime createdAt, updatedAt;
    public CommentResponse(Comment comment, String username) {
        id = comment.getId(); postId = comment.getPostId(); userId = comment.getUserId(); this.username = username;
        content = comment.getContent(); createdAt = comment.getCreatedAt(); updatedAt = comment.getUpdatedAt();
    }
    public Long getId() { return id; } public Long getPostId() { return postId; } public Long getUserId() { return userId; }
    public String getUsername() { return username; } public String getContent() { return content; }
    public LocalDateTime getCreatedAt() { return createdAt; } public LocalDateTime getUpdatedAt() { return updatedAt; }
}
