CREATE TABLE requests (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    state VARCHAR(50) NOT NULL,
    rejection_reason TEXT,
    sequence_number BIGINT UNIQUE
);

CREATE TABLE audit_logs (
    id BIGSERIAL PRIMARY KEY,
    request_id UUID NOT NULL,
    state VARCHAR(50) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE SEQUENCE request_sequence START 1 INCREMENT 1;
