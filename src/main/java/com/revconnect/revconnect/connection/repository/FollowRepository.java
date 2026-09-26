package com.revconnect.revconnect.connection.repository;

import com.revconnect.revconnect.connection.entity.Follow;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FollowRepository extends JpaRepository<Follow, Long> {

    Optional<Follow> findByFollowerIdAndFollowingId(Long followerId, Long followingId);

    boolean existsByFollowerIdAndFollowingId(Long followerId, Long followingId);

    Page<Follow> findByFollowingId(Long followingId, Pageable pageable);

    Page<Follow> findByFollowerId(Long followerId, Pageable pageable);

    long countByFollowingId(Long followingId);

    long countByFollowerId(Long followerId);
}
