CREATE TABLE IF NOT EXISTS hits (
                                    id BIGSERIAL PRIMARY KEY,
                                    app varchar NOT NULL,
                                    uri varchar NOT NULL,
                                    ip varchar NOT NULL,
                                    hit_timestamp TIMESTAMP NOT NULL
);