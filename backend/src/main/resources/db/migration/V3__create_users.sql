CREATE TABLE users (
    id            BIGINT GENERATED ALWAYS AS IDENTITY,
    username      VARCHAR(30) NOT NULL,        -- Username.MAX_LENGTH
    email         VARCHAR(254) NOT NULL,       -- Email.MAX_LENGTH
    password_hash VARCHAR(255) NOT NULL,       -- PasswordHash.MAX_LENGTH
    role          VARCHAR(20) NOT NULL,        -- enum stored by name (e.g. USER)
    created_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT pk_users PRIMARY KEY (id),
    CONSTRAINT uq_users_username UNIQUE (username),
    CONSTRAINT uq_users_email UNIQUE (email)
);

CREATE TRIGGER trg_users_set_updated_at
    BEFORE UPDATE ON users
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();
