package com.revconnect.interaction.service;

import com.revconnect.interaction.client.NotificationServiceClient;
import com.revconnect.interaction.client.PostServiceClient;
import com.revconnect.interaction.client.UserServiceClient;
import com.revconnect.interaction.client.dto.CreateNotificationRequest;
import com.revconnect.interaction.client.dto.PostResponse;
import com.revconnect.interaction.client.dto.UserProfileResponse;
import com.revconnect.interaction.dto.CommentRequest;
import com.revconnect.interaction.entity.Comment;
import com.revconnect.interaction.repository.CommentRepository;
import com.revconnect.interaction.repository.PostLikeRepository;
import com.revconnect.interaction.repository.RepostRepository;
import com.revconnect.interaction.repository.ShareRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InteractionServiceTest {

    private static final Long POST_ID = 100L;
    private static final Long OWNER_ID = 25L;
    private static final Long ACTOR_ID = 10L;

    @Mock private PostLikeRepository likes;
    @Mock private CommentRepository comments;
    @Mock private ShareRepository shares;
    @Mock private RepostRepository reposts;
    @Mock private PostServiceClient postServiceClient;
    @Mock private UserServiceClient userServiceClient;
    @Mock private NotificationServiceClient notificationServiceClient;

    private InteractionService interactionService;
    private PostResponse post;
    private UserProfileResponse actor;

    @BeforeEach
    void setUp() {
        interactionService = new InteractionService(
                likes, comments, shares, reposts,
                postServiceClient, userServiceClient, notificationServiceClient);

        post = new PostResponse();
        post.setId(POST_ID);
        post.setUserId(OWNER_ID);
        actor = new UserProfileResponse();
        actor.setUserId(ACTOR_ID);
        actor.setUsername("siddhi");

        when(postServiceClient.getPost(POST_ID)).thenReturn(post);
        lenient().when(userServiceClient.getUser(ACTOR_ID)).thenReturn(actor);
    }

    @Test
    void likeCreatesLikeNotificationForPostOwner() {
        when(likes.existsByPostIdAndUserId(POST_ID, ACTOR_ID)).thenReturn(false);

        interactionService.likePost(POST_ID, ACTOR_ID);

        verify(likes).save(any());
        verifyNotification("LIKE", "siddhi liked your post.");
    }

    @Test
    void commentCreatesCommentNotificationForPostOwner() {
        CommentRequest request = new CommentRequest();
        request.setContent("Nice post");
        when(comments.save(any(Comment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        interactionService.addComment(POST_ID, ACTOR_ID, request);

        verify(comments).save(any(Comment.class));
        verifyNotification("COMMENT", "siddhi commented on your post.");
    }

    @Test
    void shareCreatesShareNotificationForPostOwner() {
        interactionService.sharePost(POST_ID, ACTOR_ID);

        verify(shares).save(any());
        verifyNotification("SHARE", "siddhi shared your post.");
    }

    @Test
    void repostCreatesRepostNotificationForPostOwner() {
        when(reposts.existsByPostIdAndUserId(POST_ID, ACTOR_ID)).thenReturn(false);

        interactionService.repostPost(POST_ID, ACTOR_ID);

        verify(reposts).save(any());
        verifyNotification("REPOST", "siddhi reposted your post.");
    }

    @Test
    void selfInteractionDoesNotCreateNotification() {
        post.setUserId(ACTOR_ID);
        when(likes.existsByPostIdAndUserId(POST_ID, ACTOR_ID)).thenReturn(false);

        interactionService.likePost(POST_ID, ACTOR_ID);

        verify(likes).save(any());
        verifyNoInteractions(userServiceClient, notificationServiceClient);
    }

    private void verifyNotification(String type, String message) {
        ArgumentCaptor<CreateNotificationRequest> captor =
                ArgumentCaptor.forClass(CreateNotificationRequest.class);
        verify(notificationServiceClient).createNotification(captor.capture());
        CreateNotificationRequest request = captor.getValue();
        assertEquals(OWNER_ID, request.getRecipientId());
        assertEquals(ACTOR_ID, request.getActorId());
        assertEquals("siddhi", request.getActorUsername());
        assertEquals(message, request.getMessage());
        assertEquals(type, request.getType());
        assertEquals(POST_ID, request.getReferenceId());
    }
}
