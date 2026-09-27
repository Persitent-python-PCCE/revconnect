package com.revconnect.revconnect.post.service;

import com.revconnect.revconnect.connection.entity.Follow;
import com.revconnect.revconnect.connection.repository.FollowRepository;
import com.revconnect.revconnect.notification.service.NotificationService;
import com.revconnect.revconnect.post.dto.PostRequest;
import com.revconnect.revconnect.post.dto.PostResponse;
import com.revconnect.revconnect.post.entity.Post;
import com.revconnect.revconnect.post.repository.PostRepository;
import com.revconnect.revconnect.user.entity.User;
import com.revconnect.revconnect.user.repository.UserRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class PostService {

    private static final Set<String> ALLOWED_MIME_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp",
            "image/gif"
    );

    private static final int MAX_CAPTION_LENGTH = 2200;

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final FollowRepository followRepository;
    private final NotificationService notificationService;

    public PostService(
            PostRepository postRepository,
            UserRepository userRepository,
            FollowRepository followRepository,
            NotificationService notificationService) {

        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.followRepository = followRepository;
        this.notificationService = notificationService;
    }

    public PostResponse createPost(
            Long userId,
            MultipartFile photo,
            String caption) {

        if (photo == null || photo.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "A photo is required."
            );
        }

        String contentType = photo.getContentType();

        if (contentType == null
                || !ALLOWED_MIME_TYPES.contains(
                contentType.toLowerCase())) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Only image files are allowed (JPEG, PNG, WEBP, GIF)."
            );
        }

        String trimmedCaption = (caption != null)
                ? caption.trim()
                : "";

        if (trimmedCaption.length() > MAX_CAPTION_LENGTH) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Caption must not exceed "
                            + MAX_CAPTION_LENGTH
                            + " characters."
            );
        }

        String photoUrl = saveImage(photo);

        Post post = new Post();

        post.setUserId(userId);
        post.setCaption(trimmedCaption);
        post.setPhotoUrl(photoUrl);
        post.setStatus("PUBLISHED");

        Post savedPost = postRepository.save(post);

        createPostNotifications(savedPost);

        return createPostResponse(savedPost);
    }

    public PostResponse createPost(
            Long userId,
            PostRequest request) {

        throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Use multipart/form-data with a photo field to create a post."
        );
    }

    private void createPostNotifications(Post post) {

        User actor = userRepository
                .findById(post.getUserId())
                .orElse(null);

        if (actor == null) {
            return;
        }

        List<Follow> followers =
                followRepository.findByFollowingId(post.getUserId());

        for (Follow follow : followers) {

            notificationService.createNotification(
                    follow.getFollowerId(),
                    actor,
                    "created a new post.",
                    "POST_CREATED",
                    post.getId()
            );
        }
    }

    private String saveImage(MultipartFile file) {

        try {

            Path uploadDir =
                    Paths.get("uploads", "posts");

            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }

            String originalName =
                    file.getOriginalFilename();

            String extension = ".jpg";

            if (originalName != null
                    && originalName.contains(".")) {

                String ext = originalName
                        .substring(
                                originalName.lastIndexOf(".")
                        )
                        .toLowerCase();

                if (ext.equals(".jpg")
                        || ext.equals(".jpeg")
                        || ext.equals(".png")
                        || ext.equals(".webp")
                        || ext.equals(".gif")) {

                    extension = ext;
                }
            }

            String filename =
                    UUID.randomUUID().toString()
                            + extension;

            Path targetPath =
                    uploadDir.resolve(filename);

            Files.copy(
                    file.getInputStream(),
                    targetPath,
                    StandardCopyOption.REPLACE_EXISTING
            );

            return "/uploads/posts/" + filename;

        } catch (IOException e) {

            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to save image: "
                            + e.getMessage()
            );
        }
    }

    public List<PostResponse> getMyPosts(Long userId) {

        return postRepository
                .findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::createPostResponse)
                .toList();
    }

    public PostResponse getPost(Long id) {

        Post post = postRepository
                .findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Post not found"
                        )
                );

        return createPostResponse(post);
    }

    public PostResponse updatePost(
            Long id,
            Long userId,
            PostRequest request) {

        Post post = postRepository
                .findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Post not found"
                        )
                );

        if (!post.getUserId().equals(userId)) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You are not allowed to edit this post"
            );
        }

        String trimmedCaption =
                (request.getCaption() != null)
                        ? request.getCaption().trim()
                        : "";

        if (trimmedCaption.length()
                > MAX_CAPTION_LENGTH) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Caption must not exceed "
                            + MAX_CAPTION_LENGTH
                            + " characters."
            );
        }

        post.setCaption(trimmedCaption);

        Post updatedPost =
                postRepository.save(post);

        return createPostResponse(updatedPost);
    }

    public void deletePost(
            Long id,
            Long userId) {

        Post post = postRepository
                .findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Post not found"
                        )
                );

        if (!post.getUserId().equals(userId)) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You are not allowed to delete this post"
            );
        }

        postRepository.delete(post);
    }

    private PostResponse createPostResponse(Post post) {

        User user = userRepository
                .findById(post.getUserId())
                .orElse(null);

        String username = null;

        if (user != null) {
            username = user.getUsername();
        }

        return new PostResponse(
                post,
                username
        );
    }
}