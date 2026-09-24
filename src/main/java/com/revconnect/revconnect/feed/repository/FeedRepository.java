package com.revconnect.revconnect.feed.repository;

import com.revconnect.revconnect.post.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FeedRepository extends JpaRepository<Post, Long> {

    Page<Post> findByStatusOrderByCreatedAtDesc(
            String status,
            Pageable pageable
    );
}