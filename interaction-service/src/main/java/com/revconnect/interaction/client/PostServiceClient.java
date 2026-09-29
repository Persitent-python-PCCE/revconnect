package com.revconnect.interaction.client;

import com.revconnect.interaction.client.dto.PostResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "post-service")
public interface PostServiceClient {

    @GetMapping("/api/posts/{id}")
    PostResponse getPost(@PathVariable("id") Long postId);
}