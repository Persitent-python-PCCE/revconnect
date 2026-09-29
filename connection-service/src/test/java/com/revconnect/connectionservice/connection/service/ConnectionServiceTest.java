package com.revconnect.connectionservice.connection.service;

import com.revconnect.connectionservice.connection.entity.Connection;
import com.revconnect.connectionservice.connection.entity.ConnectionRequest;
import com.revconnect.connectionservice.connection.entity.ConnectionRequestStatus;
import com.revconnect.connectionservice.connection.repository.ConnectionRepository;
import com.revconnect.connectionservice.connection.repository.ConnectionRequestRepository;
import com.revconnect.connectionservice.connection.repository.FollowRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

class ConnectionServiceTest {

    @Mock
    private ConnectionRequestRepository connectionRequestRepository;

    @Mock
    private ConnectionRepository connectionRepository;

    @Mock
    private FollowRepository followRepository;

    @InjectMocks
    private ConnectionService connectionService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSendRequest_selfConnectionRejected() {
        assertThrows(ResponseStatusException.class, () -> connectionService.sendRequest(1L, 1L));
    }

    @Test
    void testSendRequest_duplicateRequestRejected() {
        when(connectionRequestRepository.findByRequesterIdAndReceiverIdAndStatus(1L, 2L, ConnectionRequestStatus.PENDING))
                .thenReturn(Optional.of(new ConnectionRequest(1L, 2L, ConnectionRequestStatus.PENDING)));

        assertThrows(ResponseStatusException.class, () -> connectionService.sendRequest(1L, 2L));
    }

    @Test
    void testSendRequest_reverseRequestDetected() {
        when(connectionRequestRepository.findByRequesterIdAndReceiverIdAndStatus(2L, 1L, ConnectionRequestStatus.PENDING))
                .thenReturn(Optional.of(new ConnectionRequest(2L, 1L, ConnectionRequestStatus.PENDING)));

        assertThrows(ResponseStatusException.class, () -> connectionService.sendRequest(1L, 2L));
    }

    @Test
    void testAcceptRequest_authorization() {
        ConnectionRequest req = new ConnectionRequest(1L, 2L, ConnectionRequestStatus.PENDING);
        when(connectionRequestRepository.findById(10L)).thenReturn(Optional.of(req));
        
        // 3L is not receiver 2L
        assertThrows(ResponseStatusException.class, () -> connectionService.acceptRequest(3L, 10L));
    }

    @Test
    void testAcceptRequest_createsConnection() {
        ConnectionRequest req = new ConnectionRequest(1L, 2L, ConnectionRequestStatus.PENDING);
        req.setId(10L);
        when(connectionRequestRepository.findById(10L)).thenReturn(Optional.of(req));
        when(connectionRepository.existsByUserOneIdAndUserTwoId(1L, 2L)).thenReturn(false);

        connectionService.acceptRequest(2L, 10L);

        verify(connectionRepository).save(any(Connection.class));
    }

    @Test
    void testFollow_duplicateFollowRejected() {
        com.revconnect.connectionservice.connection.entity.Follow f = new com.revconnect.connectionservice.connection.entity.Follow(1L, 2L);
        f.setId(5L);
        when(followRepository.findByFollowerIdAndFollowingId(1L, 2L)).thenReturn(Optional.of(f));
        
        var resp = connectionService.followUser(1L, 2L);
        assertTrue(resp.getFollowId().equals(5L));
    }
}
