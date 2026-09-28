package com.revconnect.feed.dto;

import java.util.List;

public record FeedResponse(List<PostDto> content, int page, int size, int totalPages, long totalElements, boolean last) {}
