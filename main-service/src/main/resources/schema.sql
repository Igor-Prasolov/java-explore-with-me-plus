CREATE TABLE IF NOT EXISTS users (
    id    BIGSERIAL PRIMARY KEY,
    email VARCHAR(254) NOT NULL,
    name  VARCHAR(250) NOT NULL,
    CONSTRAINT uq_email UNIQUE (email)
);

CREATE TABLE IF NOT EXISTS requests (
    id BIGSERIAL PRIMARY KEY,
    event_id BIGINT NOT NULL,
    requester_id BIGINT NOT NULL,
    created TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    status VARCHAR(50) NOT NULL,
    CONSTRAINT fk_requests_to_users FOREIGN KEY (requester_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT uq_request_event_requester UNIQUE (event_id, requester_id)
);