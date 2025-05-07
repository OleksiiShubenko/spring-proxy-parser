CREATE TABLE request_log (
    id BIGSERIAL PRIMARY KEY,
    timestamp TIMESTAMP,
    request_uri VARCHAR,
    response_status INTEGER,
    fetch_type VARCHAR
);
