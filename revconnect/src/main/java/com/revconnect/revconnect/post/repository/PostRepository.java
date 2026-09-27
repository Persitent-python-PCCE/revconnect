package com.revconnect.revconnect.post.repository;
import com.revconnect.revconnect.post.entity.Post; import org.springframework.data.domain.*; import org.springframework.data.jpa.repository.JpaRepository; import org.springframework.data.jpa.repository.Query; import java.time.LocalDateTime; import java.util.*;
public interface PostRepository extends JpaRepository<Post,Long>{
 List<Post> findByUserIdOrderByCreatedAtDesc(Long userId);
 Page<Post> findByStatusOrderByCreatedAtDesc(String status,Pageable pageable);
 List<Post> findByStatusOrderByCreatedAtDesc(String status);
 List<Post> findByStatusAndScheduledAtLessThanEqual(String status,LocalDateTime time);
}
