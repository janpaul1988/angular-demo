DROP TABLE IF EXISTS product;
DROP TABLE IF EXISTS job;
DROP TABLE IF EXISTS user;

CREATE TABLE user
(
    id    BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE
);

CREATE TABLE journal_template
(
    id      CHAR(36) PRIMARY KEY DEFAULT (UUID()),
    user_id BIGINT       NOT NULL REFERENCES user (id),
    name    varchar(255) NOT NULL,
    version int          NOT NULL,
    content JSON         NOT NULL,
    UNIQUE (user_id, name, version)
);

CREATE TABLE job
(
    id          CHAR(36) PRIMARY KEY DEFAULT (UUID()),
    user_id                  BIGINT       NOT NULL REFERENCES user (id),
    title                    VARCHAR(255) NOT NULL,
    description TEXT,
    start_date               DATE         NOT NULL,
    end_date                 DATE,
    currentJournalTemplateId CHAR(36) REFERENCES journal_template (id),
    UNIQUE (user_id, title)
);

CREATE TABLE journal
(
    id          CHAR(36) PRIMARY KEY DEFAULT (UUID()),
    job_id      CHAR(36) NOT NULL REFERENCES job (id),
    template_id CHAR(36) NOT NULL references journal_template (id),
    year        INT      NOT NULL,
    week        INT      NOT NULL,
    content     JSON     NOT NULL,
    UNIQUE (job_id, year, week),
    CHECK (week BETWEEN 1 AND 53)
);


