package com.revconnect.feed.dto;

import java.time.LocalDateTime;

public record PostDto(
        Long id,
        Long userId,
        String photoUrl,
        String caption,
        String status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}