package com.revconnect.feed.client;

import com.revconnect.feed.dto.PagedPostResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "post-service")
public interface PostServiceClient {

    @GetMapping("/api/posts/published")
    PagedPostResponse getPublishedPosts(
            @RequestParam int page,
            @RequestParam int size
    );
}