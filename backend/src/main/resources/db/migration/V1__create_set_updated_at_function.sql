-- Refreshes updated_at on UPDATE, only when the row actually changed.
CREATE FUNCTION set_updated_at() RETURNS trigger AS $$
BEGIN
    IF ROW(NEW.*) IS DISTINCT FROM ROW(OLD.*) THEN
        NEW.updated_at = now();
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;
