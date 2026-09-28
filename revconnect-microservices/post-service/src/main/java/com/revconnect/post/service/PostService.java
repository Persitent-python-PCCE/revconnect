package com.revconnect.post.service;

import com.revconnect.post.dto.PagedPostResponse;
import com.revconnect.post.dto.PostRequest;
import com.revconnect.post.dto.PostResponse;
import com.revconnect.post.entity.Post;
import com.revconnect.post.repository.PostRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    private static final Set<String> ALLOWED_MIME_TYPES = Set.of("image/jpeg", "image/png", "image/webp", "image/gif");
    private static final int MAX_CAPTION_LENGTH = 2200;

    private final PostRepository postRepository;
    private final Path uploadDir;

    public PostService(PostRepository postRepository,
                       @Value("${revconnect.post.upload-dir:uploads/posts}") String uploadDir) {
        this.postRepository = postRepository;
        this.uploadDir = Paths.get(uploadDir).toAbsolutePath().normalize();
    }

    public PostResponse createPost(Long userId, MultipartFile photo, String caption) {
        if (userId == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "X-User-Id is required");
        if (photo == null || photo.isEmpty()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A photo is required.");
        String contentType = photo.getContentType();
        if (contentType == null || !ALLOWED_MIME_TYPES.contains(contentType.toLowerCase())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only image files are allowed (JPEG, PNG, WEBP, GIF).");
        }
        String trimmedCaption = caption == null ? "" : caption.trim();
        if (trimmedCaption.length() > MAX_CAPTION_LENGTH) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Caption must not exceed " + MAX_CAPTION_LENGTH + " characters.");
        }
        Post post = new Post();
        post.setUserId(userId);
        post.setCaption(trimmedCaption);
        post.setPhotoUrl(saveImage(photo));
        post.setStatus("PUBLISHED");
        return new PostResponse(postRepository.save(post));
    }

    public List<PostResponse> getMyPosts(Long userId) {
        return postRepository.findByUserIdOrderByCreatedAtDesc(userId).stream().map(PostResponse::new).toList();
    }

    public PostResponse getPost(Long id) {
        return new PostResponse(findPost(id));
    }

    public PagedPostResponse getPublishedPosts(Pageable pageable) {
        Page<Post> page = postRepository.findByStatusOrderByCreatedAtDesc("PUBLISHED", pageable);
        return new PagedPostResponse(page.getContent().stream().map(PostResponse::new).toList(), page.getNumber(), page.getSize(), page.getTotalPages(), page.getTotalElements(), page.isLast());
    }

    public PostResponse updatePost(Long id, Long userId, PostRequest request) {
        Post post = findPost(id);
        checkOwner(post, userId);
        String caption = request.getCaption() == null ? "" : request.getCaption().trim();
        if (caption.length() > MAX_CAPTION_LENGTH) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Caption must not exceed " + MAX_CAPTION_LENGTH + " characters.");
        post.setCaption(caption);
        return new PostResponse(postRepository.save(post));
    }

    public void deletePost(Long id, Long userId) {
        Post post = findPost(id);
        checkOwner(post, userId);
        postRepository.delete(post);
    }

    private Post findPost(Long id) {
        return postRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found"));
    }

    private void checkOwner(Post post, Long userId) {
        if (userId == null || !post.getUserId().equals(userId)) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not allowed to modify this post");
    }

    private String saveImage(MultipartFile file) {
        try {
            Files.createDirectories(uploadDir);
            String original = file.getOriginalFilename();
            String extension = ".jpg";
            if (original != null && original.contains(".")) {
                String ext = original.substring(original.lastIndexOf('.')).toLowerCase();
                if (Set.of(".jpg", ".jpeg", ".png", ".webp", ".gif").contains(ext)) extension = ext;
            }
            String filename = UUID.randomUUID() + extension;
            Files.copy(file.getInputStream(), uploadDir.resolve(filename), StandardCopyOption.REPLACE_EXISTING);
            return "/uploads/posts/" + filename;
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to save image");
        }
    }
}
