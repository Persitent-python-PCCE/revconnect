package com.revconnect.revconnect.connection.repository;

import com.revconnect.revconnect.connection.entity.ConnectionRequest;
import com.revconnect.revconnect.connection.entity.ConnectionRequestStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ConnectionRequestRepository extends JpaRepository<ConnectionRequest, Long> {

    Optional<ConnectionRequest> findByRequesterIdAndReceiverIdAndStatus(Long requesterId, Long receiverId, ConnectionRequestStatus status);

    Optional<ConnectionRequest> findByRequesterIdAndReceiverId(Long requesterId, Long receiverId);

    Page<ConnectionRequest> findByReceiverIdAndStatus(Long receiverId, ConnectionRequestStatus status, Pageable pageable);

    Page<ConnectionRequest> findByRequesterIdAndStatus(Long requesterId, ConnectionRequestStatus status, Pageable pageable);

    long countByReceiverIdAndStatus(Long receiverId, ConnectionRequestStatus status);

    long countByRequesterIdAndStatus(Long requesterId, ConnectionRequestStatus status);
}
