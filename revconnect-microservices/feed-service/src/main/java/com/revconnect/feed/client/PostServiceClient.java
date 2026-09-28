package com.revconnect.feed.client;

import com.revconnect.feed.dto.PagedPostResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class PostServiceClient {
    private final RestClient restClient;

    public PostServiceClient(@Value("${revconnect.post-service.url:http://localhost:8082}") String baseUrl) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
    }

    public PagedPostResponse getPublishedPosts(int page, int size) {
        return restClient.get()
                .uri(uriBuilder -> uriBuilder.path("/api/posts/published").queryParam("page", page).queryParam("size", size).build())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(PagedPostResponse.class);
    }
}
