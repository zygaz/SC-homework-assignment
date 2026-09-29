CREATE TABLE idempotency_records (
    idempotency_key UUID PRIMARY KEY,
    response_status INT,
    response_body TEXT,
    created_at TIMESTAMP NOT NULL
);
