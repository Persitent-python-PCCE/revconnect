package com.revconnect.post.dto;

import java.util.List;

public record PagedPostResponse(
        List<PostResponse> content,
        int page,
        int size,
        int totalPages,
        long totalElements,
        boolean last) {}
