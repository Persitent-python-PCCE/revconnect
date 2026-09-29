package com.revconnect.connectionservice.connection.service;

import com.revconnect.connectionservice.connection.dto.ConnectionRequestResponse;
import com.revconnect.connectionservice.connection.dto.ConnectionResponse;
import com.revconnect.connectionservice.connection.dto.ConnectionStatsResponse;
import com.revconnect.connectionservice.connection.dto.FollowResponse;
import com.revconnect.connectionservice.connection.dto.RelationshipStatusResponse;
import com.revconnect.connectionservice.connection.entity.Connection;
import com.revconnect.connectionservice.connection.entity.ConnectionRequest;
import com.revconnect.connectionservice.connection.entity.ConnectionRequestStatus;
import com.revconnect.connectionservice.connection.entity.Follow;
import com.revconnect.connectionservice.connection.repository.ConnectionRepository;
import com.revconnect.connectionservice.connection.repository.ConnectionRequestRepository;
import com.revconnect.connectionservice.connection.repository.FollowRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@Service
public class ConnectionService {

    private final ConnectionRequestRepository connectionRequestRepository;
    private final ConnectionRepository connectionRepository;
    private final FollowRepository followRepository;

    public ConnectionService(ConnectionRequestRepository connectionRequestRepository,
                             ConnectionRepository connectionRepository,
                             FollowRepository followRepository) {
        this.connectionRequestRepository = connectionRequestRepository;
        this.connectionRepository = connectionRepository;
        this.followRepository = followRepository;
    }

    @Transactional
    public ConnectionRequestResponse sendRequest(Long currentUserId, Long targetUserId) {
        requireDifferentUsers(currentUserId, targetUserId, "send a connection request to yourself");

        Long minId = Math.min(currentUserId, targetUserId);
        Long maxId = Math.max(currentUserId, targetUserId);

        if (connectionRepository.existsByUserOneIdAndUserTwoId(minId, maxId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Already connected.");
        }

        Optional<ConnectionRequest> existingSent = connectionRequestRepository.findByRequesterIdAndReceiverIdAndStatus(currentUserId, targetUserId, ConnectionRequestStatus.PENDING);
        if (existingSent.isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Connection request already sent.");
        }

        Optional<ConnectionRequest> existingReceived = connectionRequestRepository.findByRequesterIdAndReceiverIdAndStatus(targetUserId, currentUserId, ConnectionRequestStatus.PENDING);
        if (existingReceived.isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "The other user has already sent a pending request to you.");
        }

        Optional<ConnectionRequest> oldRequest = connectionRequestRepository.findByRequesterIdAndReceiverId(currentUserId, targetUserId);
        ConnectionRequest request;
        if (oldRequest.isPresent()) {
            request = oldRequest.get();
            request.setStatus(ConnectionRequestStatus.PENDING);
        } else {
            request = new ConnectionRequest(currentUserId, targetUserId, ConnectionRequestStatus.PENDING);
        }

        request = connectionRequestRepository.save(request);

        return mapToConnectionRequestResponse(request);
    }

    @Transactional(readOnly = true)
    public Page<ConnectionRequestResponse> getReceivedRequests(Long currentUserId, Pageable pageable) {
        Page<ConnectionRequest> requests = connectionRequestRepository.findByReceiverIdAndStatus(currentUserId, ConnectionRequestStatus.PENDING, pageable);
        return requests.map(this::mapToConnectionRequestResponse);
    }

    @Transactional(readOnly = true)
    public Page<ConnectionRequestResponse> getSentRequests(Long currentUserId, Pageable pageable) {
        Page<ConnectionRequest> requests = connectionRequestRepository.findByRequesterIdAndStatus(currentUserId, ConnectionRequestStatus.PENDING, pageable);
        return requests.map(this::mapToConnectionRequestResponse);
    }

    @Transactional
    public ConnectionRequestResponse acceptRequest(Long currentUserId, Long requestId) {
        ConnectionRequest request = requireConnectionRequest(requestId);
        if (!request.getReceiverId().equals(currentUserId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the receiver can accept the connection request.");
        }
        if (request.getStatus() != ConnectionRequestStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Can only accept PENDING requests.");
        }

        request.setStatus(ConnectionRequestStatus.ACCEPTED);
        connectionRequestRepository.save(request);

        Long minId = Math.min(request.getRequesterId(), request.getReceiverId());
        Long maxId = Math.max(request.getRequesterId(), request.getReceiverId());
        if (!connectionRepository.existsByUserOneIdAndUserTwoId(minId, maxId)) {
            Connection connection = new Connection(minId, maxId);
            connectionRepository.save(connection);
        }

        Optional<ConnectionRequest> reverseReq = connectionRequestRepository.findByRequesterIdAndReceiverIdAndStatus(
                request.getReceiverId(), request.getRequesterId(), ConnectionRequestStatus.PENDING);
        reverseReq.ifPresent(req -> {
            req.setStatus(ConnectionRequestStatus.CANCELLED);
            connectionRequestRepository.save(req);
        });

        return mapToConnectionRequestResponse(request);
    }

    @Transactional
    public ConnectionRequestResponse rejectRequest(Long currentUserId, Long requestId) {
        ConnectionRequest request = requireConnectionRequest(requestId);
        if (!request.getReceiverId().equals(currentUserId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the receiver can reject the connection request.");
        }
        if (request.getStatus() != ConnectionRequestStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Can only reject PENDING requests.");
        }

        request.setStatus(ConnectionRequestStatus.REJECTED);
        connectionRequestRepository.save(request);

        return mapToConnectionRequestResponse(request);
    }

    @Transactional
    public void cancelRequest(Long currentUserId, Long requestId) {
        ConnectionRequest request = requireConnectionRequest(requestId);
        if (!request.getRequesterId().equals(currentUserId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the requester can cancel the connection request.");
        }
        if (request.getStatus() != ConnectionRequestStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Can only cancel PENDING requests.");
        }

        request.setStatus(ConnectionRequestStatus.CANCELLED);
        connectionRequestRepository.save(request);
    }

    @Transactional(readOnly = true)
    public Page<ConnectionResponse> getConnections(Long currentUserId, Pageable pageable) {
        Page<Connection> connections = connectionRepository.findAllByUserId(currentUserId, pageable);

        return connections.map(c -> {
            Long otherUserId = c.getUserOneId().equals(currentUserId) ? c.getUserTwoId() : c.getUserOneId();
            return new ConnectionResponse(c.getId(), otherUserId, null, c.getCreatedAt());
        });
    }

    @Transactional
    public void removeConnection(Long currentUserId, Long otherUserId) {
        Long minId = Math.min(currentUserId, otherUserId);
        Long maxId = Math.max(currentUserId, otherUserId);

        Optional<Connection> connection = connectionRepository.findByUserOneIdAndUserTwoId(minId, maxId);
        connection.ifPresent(connectionRepository::delete);
    }

    @Transactional
    public FollowResponse followUser(Long currentUserId, Long targetUserId) {
        requireDifferentUsers(currentUserId, targetUserId, "follow yourself");

        Optional<Follow> existingFollow = followRepository.findByFollowerIdAndFollowingId(currentUserId, targetUserId);
        if (existingFollow.isPresent()) {
            return new FollowResponse(existingFollow.get().getId(), targetUserId, null, existingFollow.get().getCreatedAt());
        }

        Follow follow = new Follow(currentUserId, targetUserId);
        follow = followRepository.save(follow);

        return new FollowResponse(follow.getId(), targetUserId, null, follow.getCreatedAt());
    }

    @Transactional
    public void unfollowUser(Long currentUserId, Long targetUserId) {
        Optional<Follow> follow = followRepository.findByFollowerIdAndFollowingId(currentUserId, targetUserId);
        follow.ifPresent(followRepository::delete);
    }

    @Transactional(readOnly = true)
    public Page<FollowResponse> getFollowers(Long currentUserId, Pageable pageable) {
        Page<Follow> follows = followRepository.findByFollowingId(currentUserId, pageable);

        return follows.map(f -> new FollowResponse(f.getId(), f.getFollowerId(), null, f.getCreatedAt()));
    }

    @Transactional(readOnly = true)
    public Page<FollowResponse> getFollowing(Long currentUserId, Pageable pageable) {
        Page<Follow> follows = followRepository.findByFollowerId(currentUserId, pageable);

        return follows.map(f -> new FollowResponse(f.getId(), f.getFollowingId(), null, f.getCreatedAt()));
    }

    @Transactional(readOnly = true)
    public RelationshipStatusResponse getRelationshipStatus(Long currentUserId, Long targetUserId) {
        String connectionStatus = "NOT_CONNECTED";
        Long minId = Math.min(currentUserId, targetUserId);
        Long maxId = Math.max(currentUserId, targetUserId);

        if (connectionRepository.existsByUserOneIdAndUserTwoId(minId, maxId)) {
            connectionStatus = "CONNECTED";
        } else if (connectionRequestRepository.findByRequesterIdAndReceiverIdAndStatus(currentUserId, targetUserId, ConnectionRequestStatus.PENDING).isPresent()) {
            connectionStatus = "PENDING_SENT";
        } else if (connectionRequestRepository.findByRequesterIdAndReceiverIdAndStatus(targetUserId, currentUserId, ConnectionRequestStatus.PENDING).isPresent()) {
            connectionStatus = "PENDING_RECEIVED";
        }

        boolean following = followRepository.existsByFollowerIdAndFollowingId(currentUserId, targetUserId);
        boolean followedBy = followRepository.existsByFollowerIdAndFollowingId(targetUserId, currentUserId);

        return new RelationshipStatusResponse(targetUserId, null, connectionStatus, following, followedBy);
    }

    @Transactional(readOnly = true)
    public ConnectionStatsResponse getStats(Long currentUserId) {
        long connections = connectionRepository.countByUserId(currentUserId);
        long followers = followRepository.countByFollowingId(currentUserId);
        long following = followRepository.countByFollowerId(currentUserId);
        long pendingReceived = connectionRequestRepository.countByReceiverIdAndStatus(currentUserId, ConnectionRequestStatus.PENDING);
        long pendingSent = connectionRequestRepository.countByRequesterIdAndStatus(currentUserId, ConnectionRequestStatus.PENDING);

        return new ConnectionStatsResponse(connections, followers, following, pendingReceived, pendingSent);
    }

    private ConnectionRequest requireConnectionRequest(Long requestId) {
        return connectionRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Connection request not found"));
    }

    private void requireDifferentUsers(Long currentUserId, Long targetUserId, String action) {
        if (currentUserId.equals(targetUserId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You cannot " + action + ".");
        }
    }

    private ConnectionRequestResponse mapToConnectionRequestResponse(ConnectionRequest request) {
        return new ConnectionRequestResponse(
                request.getId(),
                request.getRequesterId(),
                null,
                request.getReceiverId(),
                null,
                request.getStatus(),
                request.getCreatedAt(),
                request.getUpdatedAt()
        );
    }
}