CREATE TABLE posts (
                       id BIGINT PRIMARY KEY AUTO_INCREMENT,
                       user_id BIGINT NOT NULL,
                       content TEXT NOT NULL,
                       status VARCHAR(20) NOT NULL DEFAULT 'PUBLISHED',
                       created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
                           ON UPDATE CURRENT_TIMESTAMP,

                       INDEX idx_posts_user_id (user_id),
                       INDEX idx_posts_status_created_at (status, created_at)
);