package com.revconnect.revconnect.interaction.repository;

import com.revconnect.revconnect.interaction.entity.Share;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShareRepository extends JpaRepository<Share, Long> {
    long countByPostId(Long postId);
}
