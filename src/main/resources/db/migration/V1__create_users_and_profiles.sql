CREATE TABLE users (
                       id BIGINT PRIMARY KEY AUTO_INCREMENT,
                       username VARCHAR(50) NOT NULL UNIQUE,
                       email VARCHAR(100) NOT NULL UNIQUE,
                       password VARCHAR(255) NOT NULL,
                       account_type VARCHAR(20) NOT NULL
);

CREATE TABLE profiles (
                          id BIGINT PRIMARY KEY AUTO_INCREMENT,
                          user_id BIGINT NOT NULL UNIQUE,
                          full_name VARCHAR(100),
                          bio VARCHAR(500),
                          profile_picture VARCHAR(255),
                          privacy VARCHAR(20) NOT NULL,

                          CONSTRAINT fk_profile_user
                              FOREIGN KEY (user_id)
                                  REFERENCES users(id)
);