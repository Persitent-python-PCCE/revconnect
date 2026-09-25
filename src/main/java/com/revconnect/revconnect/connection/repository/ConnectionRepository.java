package com.revconnect.revconnect.connection.repository;

import com.revconnect.revconnect.connection.entity.Connection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ConnectionRepository extends JpaRepository<Connection, Long> {

    Optional<Connection> findByUserOneIdAndUserTwoId(Long userOneId, Long userTwoId);

    boolean existsByUserOneIdAndUserTwoId(Long userOneId, Long userTwoId);

    @Query("SELECT c FROM Connection c WHERE c.userOneId = :userId OR c.userTwoId = :userId ORDER BY c.createdAt DESC")
    Page<Connection> findAllByUserId(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT COUNT(c) FROM Connection c WHERE c.userOneId = :userId OR c.userTwoId = :userId")
    long countByUserId(@Param("userId") Long userId);
}
