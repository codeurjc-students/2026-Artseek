CREATE TABLE categories (
    id         BIGINT GENERATED ALWAYS AS IDENTITY,
    name       VARCHAR(255) NOT NULL,
    type       VARCHAR(255) NOT NULL,          -- enum stored by name (e.g. PAINTING)
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT pk_categories PRIMARY KEY (id),
    CONSTRAINT uq_categories_name_type UNIQUE (name, type)
);

CREATE TRIGGER trg_categories_set_updated_at
    BEFORE UPDATE ON categories
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();
