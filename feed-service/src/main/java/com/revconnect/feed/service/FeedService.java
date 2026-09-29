package com.revconnect.feed.service;

import com.revconnect.feed.client.PostServiceClient;
import com.revconnect.feed.dto.FeedResponse;
import com.revconnect.feed.dto.PagedPostResponse;
import com.revconnect.feed.dto.PostDto;
import com.revconnect.feed.entity.FeedItem;
import com.revconnect.feed.repository.FeedRepository;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FeedService {

    private final FeedRepository feedRepository;
    private final PostServiceClient postServiceClient;

    public FeedService(FeedRepository feedRepository, PostServiceClient postServiceClient) {
        this.feedRepository = feedRepository;
        this.postServiceClient = postServiceClient;
    }

    @Transactional
    public FeedResponse getFeed(int page, int size) {
        PagedPostResponse remote = postServiceClient.getPublishedPosts(page, size);
        remote.content().forEach(this::cache);

        return new FeedResponse(
                remote.content(),
                remote.page(),
                remote.size(),
                remote.totalPages(),
                remote.totalElements(),
                remote.last()
        );
    }

    private void cache(PostDto post) {
        FeedItem item = feedRepository.findByPostId(post.id());
        if (item == null) {
            item = new FeedItem();
        }

        item.setPostId(post.id());
        item.setUserId(post.userId());
        item.setPhotoUrl(post.photoUrl());
        item.setCaption(post.caption());
        item.setStatus(post.status());
        item.setPostCreatedAt(post.createdAt());
        item.setPostUpdatedAt(post.updatedAt());
        item.setCachedAt(LocalDateTime.now());

        feedRepository.save(item);
    }
}