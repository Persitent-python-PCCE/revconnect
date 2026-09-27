CREATE TABLE connection_requests (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    requester_id BIGINT NOT NULL,
    receiver_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_connection_requests_requester FOREIGN KEY (requester_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_connection_requests_receiver FOREIGN KEY (receiver_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE (requester_id, receiver_id)
);

CREATE INDEX idx_connection_requests_requester_status ON connection_requests(requester_id, status);
CREATE INDEX idx_connection_requests_receiver_status ON connection_requests(receiver_id, status);

CREATE TABLE connections (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_one_id BIGINT NOT NULL,
    user_two_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_connections_user_one FOREIGN KEY (user_one_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_connections_user_two FOREIGN KEY (user_two_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE (user_one_id, user_two_id)
);

CREATE INDEX idx_connections_user_one ON connections(user_one_id);
CREATE INDEX idx_connections_user_two ON connections(user_two_id);

CREATE TABLE follows (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    follower_id BIGINT NOT NULL,
    following_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_follows_follower FOREIGN KEY (follower_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_follows_following FOREIGN KEY (following_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE (follower_id, following_id)
);

CREATE INDEX idx_follows_follower ON follows(follower_id);
CREATE INDEX idx_follows_following ON follows(following_id);
