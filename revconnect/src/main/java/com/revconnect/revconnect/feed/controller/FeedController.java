package com.revconnect.revconnect.feed.controller;

import com.revconnect.revconnect.feed.dto.FeedResponse;
import com.revconnect.revconnect.feed.service.FeedService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/feed")
public class FeedController {

    private final FeedService feedService;

    public FeedController(FeedService feedService) {
        this.feedService = feedService;
    }

    @GetMapping
    public ResponseEntity<FeedResponse> get(
            Authentication authentication,
            @RequestParam(defaultValue = "latest") String type,
            @RequestParam(required = false) String hashtag,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {

        return ResponseEntity.ok(
                feedService.getFeed(
                        (Long) authentication.getPrincipal(),
                        type,
                        hashtag,
                        Math.max(0, page),
                        Math.min(20, Math.max(1, size))
                )
        );
    }
}