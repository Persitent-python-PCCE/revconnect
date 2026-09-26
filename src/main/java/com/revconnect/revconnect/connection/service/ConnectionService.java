package com.revconnect.revconnect.connection.service;

import com.revconnect.revconnect.connection.dto.ConnectionRequestResponse;
import com.revconnect.revconnect.connection.dto.ConnectionResponse;
import com.revconnect.revconnect.connection.dto.ConnectionStatsResponse;
import com.revconnect.revconnect.connection.dto.FollowResponse;
import com.revconnect.revconnect.connection.dto.RelationshipStatusResponse;
import com.revconnect.revconnect.connection.entity.Connection;
import com.revconnect.revconnect.connection.entity.ConnectionRequest;
import com.revconnect.revconnect.connection.entity.ConnectionRequestStatus;
import com.revconnect.revconnect.connection.entity.Follow;
import com.revconnect.revconnect.connection.repository.ConnectionRepository;
import com.revconnect.revconnect.connection.repository.ConnectionRequestRepository;
import com.revconnect.revconnect.connection.repository.FollowRepository;
import com.revconnect.revconnect.notification.service.NotificationService;
import com.revconnect.revconnect.user.entity.User;
import com.revconnect.revconnect.user.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ConnectionService {

    private final ConnectionRequestRepository connectionRequestRepository;
    private final ConnectionRepository connectionRepository;
    private final FollowRepository followRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public ConnectionService(ConnectionRequestRepository connectionRequestRepository,
                             ConnectionRepository connectionRepository,
                             FollowRepository followRepository,
                             UserRepository userRepository,
                             NotificationService notificationService) {
        this.connectionRequestRepository = connectionRequestRepository;
        this.connectionRepository = connectionRepository;
        this.followRepository = followRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    @Transactional
    public ConnectionRequestResponse sendRequest(Long currentUserId, Long targetUserId) {
        requireDifferentUsers(currentUserId, targetUserId, "send a connection request to yourself");
        User targetUser = requireUser(targetUserId);

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

        User currentUser = requireUser(currentUserId);
        
        notificationService.createNotification(
                targetUserId,
                currentUser,
                "sent you a connection request.",
                "CONNECTION_REQUEST",
                request.getId()
        );

        return mapToConnectionRequestResponse(request, currentUser, targetUser);
    }

    @Transactional(readOnly = true)
    public Page<ConnectionRequestResponse> getReceivedRequests(Long currentUserId, Pageable pageable) {
        Page<ConnectionRequest> requests = connectionRequestRepository.findByReceiverIdAndStatus(currentUserId, ConnectionRequestStatus.PENDING, pageable);
        return mapRequests(requests);
    }

    @Transactional(readOnly = true)
    public Page<ConnectionRequestResponse> getSentRequests(Long currentUserId, Pageable pageable) {
        Page<ConnectionRequest> requests = connectionRequestRepository.findByRequesterIdAndStatus(currentUserId, ConnectionRequestStatus.PENDING, pageable);
        return mapRequests(requests);
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

        // Rule 11: Cleanup reverse pending request if it exists
        Optional<ConnectionRequest> reverseReq = connectionRequestRepository.findByRequesterIdAndReceiverIdAndStatus(
                request.getReceiverId(), request.getRequesterId(), ConnectionRequestStatus.PENDING);
        reverseReq.ifPresent(req -> {
            req.setStatus(ConnectionRequestStatus.CANCELLED);
            connectionRequestRepository.save(req);
        });

        User requester = requireUser(request.getRequesterId());
        User receiver = requireUser(request.getReceiverId());
        
        notificationService.deleteConnectionRequestNotification(request.getId());
        
        return mapToConnectionRequestResponse(request, requester, receiver);
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

        User requester = requireUser(request.getRequesterId());
        User receiver = requireUser(request.getReceiverId());
        
        notificationService.deleteConnectionRequestNotification(request.getId());
        
        return mapToConnectionRequestResponse(request, requester, receiver);
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
        
        notificationService.deleteConnectionRequestNotification(request.getId());
    }

    @Transactional(readOnly = true)
    public Page<ConnectionResponse> getConnections(Long currentUserId, Pageable pageable) {
        Page<Connection> connections = connectionRepository.findAllByUserId(currentUserId, pageable);
        List<Long> otherUserIds = connections.getContent().stream()
                .map(c -> c.getUserOneId().equals(currentUserId) ? c.getUserTwoId() : c.getUserOneId())
                .collect(Collectors.toList());

        Map<Long, User> usersMap = userRepository.findAllById(otherUserIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u));

        return connections.map(c -> {
            Long otherUserId = c.getUserOneId().equals(currentUserId) ? c.getUserTwoId() : c.getUserOneId();
            User otherUser = usersMap.get(otherUserId);
            return new ConnectionResponse(c.getId(), otherUserId, otherUser != null ? otherUser.getUsername() : null, c.getCreatedAt());
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
        User targetUser = requireUser(targetUserId);

        Optional<Follow> existingFollow = followRepository.findByFollowerIdAndFollowingId(currentUserId, targetUserId);
        if (existingFollow.isPresent()) {
            return new FollowResponse(existingFollow.get().getId(), targetUser.getId(), targetUser.getUsername(), existingFollow.get().getCreatedAt());
        }

        Follow follow = new Follow(currentUserId, targetUserId);
        follow = followRepository.save(follow);
        
        User currentUser = requireUser(currentUserId);
        notificationService.createNotification(
                targetUserId,
                currentUser,
                "followed you.",
                "FOLLOW",
                follow.getId()
        );

        return new FollowResponse(follow.getId(), targetUser.getId(), targetUser.getUsername(), follow.getCreatedAt());
    }

    @Transactional
    public void unfollowUser(Long currentUserId, Long targetUserId) {
        Optional<Follow> follow = followRepository.findByFollowerIdAndFollowingId(currentUserId, targetUserId);
        follow.ifPresent(followRepository::delete);
    }

    @Transactional(readOnly = true)
    public Page<FollowResponse> getFollowers(Long currentUserId, Pageable pageable) {
        Page<Follow> follows = followRepository.findByFollowingId(currentUserId, pageable);
        List<Long> followerIds = follows.getContent().stream().map(Follow::getFollowerId).collect(Collectors.toList());
        Map<Long, User> usersMap = userRepository.findAllById(followerIds).stream().collect(Collectors.toMap(User::getId, u -> u));
        
        return follows.map(f -> {
            User user = usersMap.get(f.getFollowerId());
            return new FollowResponse(f.getId(), f.getFollowerId(), user != null ? user.getUsername() : null, f.getCreatedAt());
        });
    }

    @Transactional(readOnly = true)
    public Page<FollowResponse> getFollowing(Long currentUserId, Pageable pageable) {
        Page<Follow> follows = followRepository.findByFollowerId(currentUserId, pageable);
        List<Long> followingIds = follows.getContent().stream().map(Follow::getFollowingId).collect(Collectors.toList());
        Map<Long, User> usersMap = userRepository.findAllById(followingIds).stream().collect(Collectors.toMap(User::getId, u -> u));
        
        return follows.map(f -> {
            User user = usersMap.get(f.getFollowingId());
            return new FollowResponse(f.getId(), f.getFollowingId(), user != null ? user.getUsername() : null, f.getCreatedAt());
        });
    }

    @Transactional(readOnly = true)
    public RelationshipStatusResponse getRelationshipStatus(Long currentUserId, Long targetUserId) {
        User targetUser = requireUser(targetUserId);
        
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

        return new RelationshipStatusResponse(targetUser.getId(), targetUser.getUsername(), connectionStatus, following, followedBy);
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

    private User requireUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
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

    private Page<ConnectionRequestResponse> mapRequests(Page<ConnectionRequest> requests) {
        List<Long> userIds = requests.getContent().stream()
                .flatMap(req -> java.util.stream.Stream.of(req.getRequesterId(), req.getReceiverId()))
                .distinct()
                .collect(Collectors.toList());

        Map<Long, User> usersMap = userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u));

        return requests.map(req -> {
            User requester = usersMap.get(req.getRequesterId());
            User receiver = usersMap.get(req.getReceiverId());
            return mapToConnectionRequestResponse(req, requester, receiver);
        });
    }

    private ConnectionRequestResponse mapToConnectionRequestResponse(ConnectionRequest request, User requester, User receiver) {
        return new ConnectionRequestResponse(
                request.getId(),
                request.getRequesterId(),
                requester != null ? requester.getUsername() : null,
                request.getReceiverId(),
                receiver != null ? receiver.getUsername() : null,
                request.getStatus(),
                request.getCreatedAt(),
                request.getUpdatedAt()
        );
    }
}
