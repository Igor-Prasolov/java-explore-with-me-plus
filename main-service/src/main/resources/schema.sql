CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(254) NOT NULL,
    name  VARCHAR(250) NOT NULL,
    CONSTRAINT uq_email UNIQUE (email)
);

CREATE TABLE IF NOT EXISTS events (
    id BIGSERIAL PRIMARY KEY,
    annotation varchar(2000) NOT NULL,
    description varchar(7000) NOT NULL,
    event_date TIMESTAMP NOT NULL,
    lat DOUBLE PRECISION NOT NULL,
    lon DOUBLE PRECISION NOT NULL,
    title varchar NOT NULL,
    created_on TIMESTAMP NOT NULL,
    published_on TIMESTAMP,
    event_state varchar,
    is_paid BOOLEAN,
    is_request_moderation BOOLEAN,
    participant_limit INTEGER,
    category_id BIGINT REFERENCES categories(id) NOT NULL,
    initiator_id BIGINT REFERENCES users(id) NOT NULL
);
