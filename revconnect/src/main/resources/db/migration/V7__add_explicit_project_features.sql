ALTER TABLE profiles ADD COLUMN location VARCHAR(150);
ALTER TABLE profiles ADD COLUMN website VARCHAR(255);

CREATE TABLE products (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    business_id BIGINT NOT NULL,
    name VARCHAR(120) NOT NULL,
    description VARCHAR(500),
    price DECIMAL(12,2),
    image_url VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_products_business FOREIGN KEY (business_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_products_business_id (business_id)
);

ALTER TABLE posts ADD COLUMN hashtags TEXT;
ALTER TABLE posts ADD COLUMN visibility VARCHAR(20) NOT NULL DEFAULT 'PUBLIC';
ALTER TABLE posts ADD COLUMN is_promotional BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE posts ADD COLUMN cta_text VARCHAR(100);
ALTER TABLE posts ADD COLUMN cta_url VARCHAR(500);
ALTER TABLE posts ADD COLUMN product_id BIGINT NULL;
ALTER TABLE posts ADD COLUMN scheduled_at TIMESTAMP NULL;
ALTER TABLE posts ADD COLUMN pinned BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE posts ADD CONSTRAINT fk_posts_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE SET NULL;
CREATE INDEX idx_posts_visibility ON posts(visibility);
CREATE INDEX idx_posts_scheduled_at ON posts(status, scheduled_at);

CREATE TABLE notification_preferences (
    user_id BIGINT PRIMARY KEY,
    likes BOOLEAN NOT NULL DEFAULT TRUE,
    comments BOOLEAN NOT NULL DEFAULT TRUE,
    follows BOOLEAN NOT NULL DEFAULT TRUE,
    connections BOOLEAN NOT NULL DEFAULT TRUE,
    reposts BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT fk_notification_preferences_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
