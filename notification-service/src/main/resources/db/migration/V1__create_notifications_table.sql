CREATE TABLE notifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    recipient_id BIGINT NOT NULL,
    actor_id BIGINT NULL,
    actor_username VARCHAR(100) NULL,
    message VARCHAR(255) NOT NULL,
    type VARCHAR(50) NOT NULL,
    reference_id BIGINT NULL,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_notifications_recipient_created (recipient_id, created_at),
    INDEX idx_notifications_recipient_read (recipient_id, is_read),
    INDEX idx_notifications_type_reference (type, reference_id)
);
