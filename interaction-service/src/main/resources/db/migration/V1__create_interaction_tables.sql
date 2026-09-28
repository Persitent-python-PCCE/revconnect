CREATE TABLE post_likes (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    post_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_post_likes_post_user UNIQUE (post_id, user_id),
    INDEX idx_post_likes_post_id (post_id),
    INDEX idx_post_likes_user_id (user_id)
);

CREATE TABLE post_comments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    post_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    content TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_post_comments_post_created (post_id, created_at),
    INDEX idx_post_comments_user_id (user_id)
);

CREATE TABLE post_shares (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    post_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_post_shares_post_id (post_id),
    INDEX idx_post_shares_user_id (user_id)
);

CREATE TABLE post_reposts (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    post_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_post_reposts_post_user UNIQUE (post_id, user_id),
    INDEX idx_post_reposts_post_id (post_id),
    INDEX idx_post_reposts_user_id (user_id)
);
