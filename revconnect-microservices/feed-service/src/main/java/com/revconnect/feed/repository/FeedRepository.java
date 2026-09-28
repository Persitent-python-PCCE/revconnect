package com.revconnect.feed.repository;

import com.revconnect.feed.entity.FeedItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FeedRepository extends JpaRepository<FeedItem, Long> {
    Page<FeedItem> findByStatusOrderByPostCreatedAtDesc(String status, Pageable pageable);
    FeedItem findByPostId(Long postId);
}
