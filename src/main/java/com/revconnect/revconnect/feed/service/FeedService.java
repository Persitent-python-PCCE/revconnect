package com.revconnect.revconnect.feed.service;

import com.revconnect.revconnect.feed.dto.FeedResponse;
import com.revconnect.revconnect.feed.repository.FeedRepository;
import com.revconnect.revconnect.post.dto.PostResponse;
import com.revconnect.revconnect.post.entity.Post;
import com.revconnect.revconnect.user.entity.User;
import com.revconnect.revconnect.user.repository.UserRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FeedService {

    private final FeedRepository feedRepository;
    private final UserRepository userRepository;

    public FeedService(
            FeedRepository feedRepository,
            UserRepository userRepository) {

        this.feedRepository = feedRepository;
        this.userRepository = userRepository;
    }

    public FeedResponse getFeed(Pageable pageable) {

        Page<Post> postPage =
                feedRepository.findByStatusOrderByCreatedAtDesc(
                        "PUBLISHED",
                        pageable
                );

        List<PostResponse> posts =
                postPage.getContent()
                        .stream()
                        .map(this::createPostResponse)
                        .toList();

        return new FeedResponse(
                posts,
                postPage.getNumber(),
                postPage.getSize(),
                postPage.getTotalPages(),
                postPage.getTotalElements(),
                postPage.isLast()
        );
    }

    private PostResponse createPostResponse(Post post) {

        User user = userRepository
                .findById(post.getUserId())
                .orElse(null);

        String username = null;

        if (user != null) {
            username = user.getUsername();
        }

        return new PostResponse(
                post,
                username
        );
    }
}