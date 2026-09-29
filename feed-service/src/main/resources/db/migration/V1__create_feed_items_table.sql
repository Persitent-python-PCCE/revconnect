CREATE TABLE feed_items (
    id BIGINT NOT NULL AUTO_INCREMENT,
    post_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    photo_url VARCHAR(500) NOT NULL,
    caption TEXT NOT NULL,
    status VARCHAR(20) NOT NULL,
    post_created_at DATETIME NOT NULL,
    post_updated_at DATETIME NOT NULL,
    cached_at DATETIME NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_feed_post_id (post_id),
    INDEX idx_feed_created (post_created_at)
);
