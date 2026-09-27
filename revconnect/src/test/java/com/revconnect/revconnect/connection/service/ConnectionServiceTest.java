package com.revconnect.revconnect.connection.service;

import com.revconnect.revconnect.connection.dto.ConnectionRequestResponse;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ConnectionServiceTest {

    @Mock
    private ConnectionRequestRepository connectionRequestRepository;

    @Mock
    private ConnectionRepository connectionRepository;

    @Mock
    private FollowRepository followRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private ConnectionService connectionService;

    private User userA;
    private User userB;

    @BeforeEach
    void setUp() {
        userA = new User();
        userA.setId(1L);
        userA.setUsername("userA");

        userB = new User();
        userB.setId(2L);
        userB.setUsername("userB");
    }

    // 1. Cannot send connection request to self
    @Test
    void sendRequest_toSelf_throwsBadRequest() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> connectionService.sendRequest(1L, 1L));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertTrue(ex.getReason().contains("yourself"));
    }

    // 2. Cannot send request to nonexistent user
    @Test
    void sendRequest_nonExistentUser_throwsNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> connectionService.sendRequest(1L, 99L));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    // 3. Cannot send duplicate pending request
    @Test
    void sendRequest_duplicatePendingRequest_throwsConflict() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(userB));
        when(connectionRepository.existsByUserOneIdAndUserTwoId(1L, 2L)).thenReturn(false);
        when(connectionRequestRepository.findByRequesterIdAndReceiverIdAndStatus(1L, 2L, ConnectionRequestStatus.PENDING))
                .thenReturn(Optional.of(new ConnectionRequest(1L, 2L, ConnectionRequestStatus.PENDING)));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> connectionService.sendRequest(1L, 2L));
        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
        assertTrue(ex.getReason().contains("already sent"));
    }

    // 4. Cannot request an already-connected user
    @Test
    void sendRequest_alreadyConnected_throwsConflict() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(userB));
        when(connectionRepository.existsByUserOneIdAndUserTwoId(1L, 2L)).thenReturn(true);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> connectionService.sendRequest(1L, 2L));
        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
        assertTrue(ex.getReason().contains("Already connected"));
    }

    // 5. Successfully create a connection request
    @Test
    void sendRequest_success() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(userB));
        when(userRepository.findById(1L)).thenReturn(Optional.of(userA));
        when(connectionRepository.existsByUserOneIdAndUserTwoId(1L, 2L)).thenReturn(false);
        when(connectionRequestRepository.findByRequesterIdAndReceiverIdAndStatus(1L, 2L, ConnectionRequestStatus.PENDING)).thenReturn(Optional.empty());
        when(connectionRequestRepository.findByRequesterIdAndReceiverIdAndStatus(2L, 1L, ConnectionRequestStatus.PENDING)).thenReturn(Optional.empty());
        when(connectionRequestRepository.findByRequesterIdAndReceiverId(1L, 2L)).thenReturn(Optional.empty());

        ConnectionRequest savedRequest = new ConnectionRequest(1L, 2L, ConnectionRequestStatus.PENDING);
        savedRequest.setId(10L);
        when(connectionRequestRepository.save(any(ConnectionRequest.class))).thenReturn(savedRequest);

        ConnectionRequestResponse response = connectionService.sendRequest(1L, 2L);
        assertNotNull(response);
        assertEquals(10L, response.getRequestId());
        assertEquals(ConnectionRequestStatus.PENDING, response.getStatus());

        verify(notificationService, times(1)).createNotification(
                2L,
                userA,
                "sent you a connection request.",
                "CONNECTION_REQUEST",
                10L
        );
    }

    // 6. Only receiver can accept
    @Test
    void acceptRequest_notReceiver_throwsForbidden() {
        ConnectionRequest req = new ConnectionRequest(1L, 2L, ConnectionRequestStatus.PENDING);
        req.setId(10L);
        when(connectionRequestRepository.findById(10L)).thenReturn(Optional.of(req));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> connectionService.acceptRequest(1L, 10L)); // requester tries to accept
        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
    }

    // 7. Only receiver can reject
    @Test
    void rejectRequest_notReceiver_throwsForbidden() {
        ConnectionRequest req = new ConnectionRequest(1L, 2L, ConnectionRequestStatus.PENDING);
        req.setId(10L);
        when(connectionRequestRepository.findById(10L)).thenReturn(Optional.of(req));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> connectionService.rejectRequest(1L, 10L)); // requester tries to reject
        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
    }

    // 8. Only requester can cancel
    @Test
    void cancelRequest_notRequester_throwsForbidden() {
        ConnectionRequest req = new ConnectionRequest(1L, 2L, ConnectionRequestStatus.PENDING);
        req.setId(10L);
        when(connectionRequestRepository.findById(10L)).thenReturn(Optional.of(req));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> connectionService.cancelRequest(2L, 10L)); // receiver tries to cancel
        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
    }

    // 9. Accepting request creates an active connection & 10. changes status to ACCEPTED
    @Test
    void acceptRequest_success() {
        ConnectionRequest req = new ConnectionRequest(1L, 2L, ConnectionRequestStatus.PENDING);
        req.setId(10L);
        when(connectionRequestRepository.findById(10L)).thenReturn(Optional.of(req));
        when(connectionRepository.existsByUserOneIdAndUserTwoId(1L, 2L)).thenReturn(false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(userA));
        when(userRepository.findById(2L)).thenReturn(Optional.of(userB));

        when(connectionRequestRepository.findByRequesterIdAndReceiverIdAndStatus(2L, 1L, ConnectionRequestStatus.PENDING))
                .thenReturn(Optional.empty());

        ConnectionRequestResponse res = connectionService.acceptRequest(2L, 10L); // receiver accepts

        assertEquals(ConnectionRequestStatus.ACCEPTED, req.getStatus());
        verify(connectionRequestRepository, times(1)).save(req);
        verify(connectionRepository, times(1)).save(any(Connection.class)); // active connection created
        assertEquals(ConnectionRequestStatus.ACCEPTED, res.getStatus());

        verify(notificationService, times(1)).deleteConnectionRequestNotification(10L);
        verify(notificationService, times(1)).createNotification(
                1L,
                userB,
                "accepted your connection request.",
                "CONNECTION_ACCEPTED",
                10L
        );
    }

    // 11. Invalid request state transition returns conflict
    @Test
    void acceptRequest_notPending_throwsConflict() {
        ConnectionRequest req = new ConnectionRequest(1L, 2L, ConnectionRequestStatus.REJECTED);
        req.setId(10L);
        when(connectionRequestRepository.findById(10L)).thenReturn(Optional.of(req));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> connectionService.acceptRequest(2L, 10L));
        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
    }

    // 12. Removing a connection removes the active relationship
    @Test
    void removeConnection_success() {
        Connection conn = new Connection(1L, 2L);
        when(connectionRepository.findByUserOneIdAndUserTwoId(1L, 2L)).thenReturn(Optional.of(conn));

        connectionService.removeConnection(1L, 2L);

        verify(connectionRepository, times(1)).delete(conn);
    }

    // 13. Follow cannot target self
    @Test
    void followUser_self_throwsBadRequest() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> connectionService.followUser(1L, 1L));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    // 14. Follow succeeds
    @Test
    void followUser_success() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(userB));
        when(userRepository.findById(1L)).thenReturn(Optional.of(userA));
        when(followRepository.findByFollowerIdAndFollowingId(1L, 2L)).thenReturn(Optional.empty());

        Follow follow = new Follow(1L, 2L);
        follow.setId(5L);
        follow.setCreatedAt(LocalDateTime.now());
        when(followRepository.save(any(Follow.class))).thenReturn(follow);

        FollowResponse res = connectionService.followUser(1L, 2L);

        assertNotNull(res);
        assertEquals(5L, res.getFollowId());

        verify(notificationService, times(1)).createNotification(
                2L,
                userA,
                "followed you.",
                "FOLLOW",
                5L
        );
    }

    // 15. Duplicate follow does not create duplicate row
    @Test
    void followUser_duplicate_returnsExisting() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(userB));
        Follow existing = new Follow(1L, 2L);
        existing.setId(5L);
        when(followRepository.findByFollowerIdAndFollowingId(1L, 2L)).thenReturn(Optional.of(existing));

        FollowResponse res = connectionService.followUser(1L, 2L);

        assertEquals(5L, res.getFollowId());
        verify(followRepository, never()).save(any(Follow.class));
        verifyNoInteractions(notificationService);
    }

    // 16. Unfollow works
    @Test
    void unfollowUser_success() {
        Follow existing = new Follow(1L, 2L);
        when(followRepository.findByFollowerIdAndFollowingId(1L, 2L)).thenReturn(Optional.of(existing));

        connectionService.unfollowUser(1L, 2L);

        verify(followRepository, times(1)).delete(existing);
    }

    // 17. Relationship status correctly reports PENDING_SENT
    @Test
    void getRelationshipStatus_pendingSent() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(userB));
        when(connectionRepository.existsByUserOneIdAndUserTwoId(1L, 2L)).thenReturn(false);
        when(connectionRequestRepository.findByRequesterIdAndReceiverIdAndStatus(1L, 2L, ConnectionRequestStatus.PENDING))
                .thenReturn(Optional.of(new ConnectionRequest()));

        RelationshipStatusResponse res = connectionService.getRelationshipStatus(1L, 2L);
        assertEquals("PENDING_SENT", res.getConnectionStatus());
    }

    // 18. Relationship status correctly reports PENDING_RECEIVED
    @Test
    void getRelationshipStatus_pendingReceived() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(userB));
        when(connectionRepository.existsByUserOneIdAndUserTwoId(1L, 2L)).thenReturn(false);
        when(connectionRequestRepository.findByRequesterIdAndReceiverIdAndStatus(1L, 2L, ConnectionRequestStatus.PENDING))
                .thenReturn(Optional.empty());
        when(connectionRequestRepository.findByRequesterIdAndReceiverIdAndStatus(2L, 1L, ConnectionRequestStatus.PENDING))
                .thenReturn(Optional.of(new ConnectionRequest()));

        RelationshipStatusResponse res = connectionService.getRelationshipStatus(1L, 2L);
        assertEquals("PENDING_RECEIVED", res.getConnectionStatus());
    }

    // 19. Relationship status correctly reports CONNECTED
    @Test
    void getRelationshipStatus_connected() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(userB));
        when(connectionRepository.existsByUserOneIdAndUserTwoId(1L, 2L)).thenReturn(true);

        RelationshipStatusResponse res = connectionService.getRelationshipStatus(1L, 2L);
        assertEquals("CONNECTED", res.getConnectionStatus());
    }

    // 20. Statistics return correct counts
    @Test
    void getStats_success() {
        when(connectionRepository.countByUserId(1L)).thenReturn(10L);
        when(followRepository.countByFollowingId(1L)).thenReturn(5L);
        when(followRepository.countByFollowerId(1L)).thenReturn(8L);
        when(connectionRequestRepository.countByReceiverIdAndStatus(1L, ConnectionRequestStatus.PENDING)).thenReturn(2L);
        when(connectionRequestRepository.countByRequesterIdAndStatus(1L, ConnectionRequestStatus.PENDING)).thenReturn(3L);

        ConnectionStatsResponse res = connectionService.getStats(1L);

        assertEquals(10L, res.getConnectionCount());
        assertEquals(5L, res.getFollowerCount());
        assertEquals(8L, res.getFollowingCount());
        assertEquals(2L, res.getPendingReceivedCount());
        assertEquals(3L, res.getPendingSentCount());
    }
}