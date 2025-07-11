DROP TABLE IF EXISTS job;
DROP TABLE IF EXISTS user;
CREATE TABLE user
(
    id    BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) UNIQUE
);
CREATE TABLE job
(
    id          CHAR(36) PRIMARY KEY DEFAULT (UUID()),
    user_id     BIGINT NOT NULL REFERENCES user (id),
    external_id BIGINT NOT NULL,
    title     VARCHAR(255) NOT NULL,
    description TEXT,
    startDate DATE         NOT NULL,
    endDate   DATE,
    UNIQUE (user_id, external_id)
);



