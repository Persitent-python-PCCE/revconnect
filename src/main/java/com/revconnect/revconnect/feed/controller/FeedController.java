package com.revconnect.revconnect.feed.controller;

import com.revconnect.revconnect.feed.dto.FeedResponse;
import com.revconnect.revconnect.feed.service.FeedService;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/feed")
public class FeedController {

    private final FeedService feedService;

    public FeedController(FeedService feedService) {
        this.feedService = feedService;
    }

    /**
     * Get published posts for the feed.
     *
     * Example:
     * /api/feed?page=0&size=10
     *
     * page = page number starting from 0
     * size = number of posts per page
     *
     * Posts are returned newest first.
     */
    @GetMapping
    public ResponseEntity<FeedResponse> getFeed(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        int pageNumber = Math.max(page, 0);

        int pageSize = Math.min(
                Math.max(size, 1),
                20
        );

        Pageable pageable = PageRequest.of(
                pageNumber,
                pageSize,
                Sort.by(
                        Sort.Direction.DESC,
                        "createdAt"
                )
        );

        return ResponseEntity.ok(
                feedService.getFeed(pageable)
        );
    }
}