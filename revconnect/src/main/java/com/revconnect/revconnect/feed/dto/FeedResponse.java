package com.revconnect.revconnect.feed.dto;

import com.revconnect.revconnect.post.dto.PostResponse;

import java.util.List;

public class FeedResponse {

    private List<PostResponse> content;
    private int page;
    private int size;
    private int totalPages;
    private long totalElements;
    private boolean last;

    public FeedResponse(
            List<PostResponse> content,
            int page,
            int size,
            int totalPages,
            long totalElements,
            boolean last) {

        this.content = content;
        this.page = page;
        this.size = size;
        this.totalPages = totalPages;
        this.totalElements = totalElements;
        this.last = last;
    }

    public List<PostResponse> getContent() {
        return content;
    }

    public int getPage() {
        return page;
    }

    public int getSize() {
        return size;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public long getTotalElements() {
        return totalElements;
    }

    public boolean isLast() {
        return last;
    }
}