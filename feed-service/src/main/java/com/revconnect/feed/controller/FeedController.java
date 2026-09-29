package com.revconnect.feed.controller;

import com.revconnect.feed.dto.FeedResponse;
import com.revconnect.feed.service.FeedService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/feed")
public class FeedController {

    private final FeedService feedService;

    public FeedController(FeedService feedService) {
        this.feedService = feedService;
    }

    @GetMapping
    public ResponseEntity<FeedResponse> getFeed(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        int pageNumber = Math.max(page, 0);
        int pageSize = Math.min(Math.max(size, 1), 20);

        return ResponseEntity.ok(feedService.getFeed(pageNumber, pageSize));
    }
}