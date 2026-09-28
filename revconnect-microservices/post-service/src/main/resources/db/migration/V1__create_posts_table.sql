CREATE TABLE posts (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    photo_url VARCHAR(500) NOT NULL,
    caption TEXT NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    PRIMARY KEY (id),
    INDEX idx_posts_user_created (user_id, created_at),
    INDEX idx_posts_status_created (status, created_at)
);
