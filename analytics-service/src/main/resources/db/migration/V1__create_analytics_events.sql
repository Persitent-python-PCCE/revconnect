CREATE TABLE analytics_events (
 id BIGINT AUTO_INCREMENT PRIMARY KEY,
 owner_id BIGINT NOT NULL,
 owner_type VARCHAR(30) NOT NULL,
 metric_type VARCHAR(50) NOT NULL,
 post_id BIGINT NULL,
 metric_value BIGINT NOT NULL DEFAULT 1
);
CREATE INDEX idx_analytics_owner ON analytics_events(owner_id, owner_type);
CREATE INDEX idx_analytics_metric ON analytics_events(owner_id, owner_type, metric_type);
