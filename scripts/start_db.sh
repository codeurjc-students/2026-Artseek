#!/usr/bin/env bash
# Starts the local development PostgreSQL container until Docker Compose replaces it.
#
# Usage (from the repository root):
#   scripts/start_db.sh           Create the container if missing, start it, wait until ready.
#   scripts/start_db.sh --reset   Remove the container and its volume first (all local data is lost).
#
# The schema is created by Flyway when the backend starts; sample data comes from
# DatabaseInitializer with the `local` profile.

set -euo pipefail

CONTAINER_NAME="${DB_CONTAINER_NAME:-postgresDb}"
VOLUME_NAME="${DB_VOLUME_NAME:-artseek_postgres_data}"
IMAGE="${DB_IMAGE:-postgres:17.11}"
DB_NAME="${DB_NAME:-artseek}"
DB_USERNAME="${DB_USERNAME:-artseek}"
DB_PASSWORD="${DB_PASSWORD:-artseek_dev_password}"
DB_PORT="${DB_PORT:-5432}"

reset=false
case "${1:-}" in
    "") ;;
    --reset) reset=true ;;
    *) echo "Usage: $0 [--reset]" >&2; exit 2 ;;
esac

if ! docker info >/dev/null 2>&1; then
    echo "Docker is not running. Start Docker and try again." >&2
    exit 1
fi

container_exists() {
    docker container inspect "$CONTAINER_NAME" >/dev/null 2>&1
}

if $reset; then
    echo "Removing container '$CONTAINER_NAME' and volume '$VOLUME_NAME'..."
    if container_exists; then
        docker rm --force "$CONTAINER_NAME" >/dev/null
    fi
    docker volume rm --force "$VOLUME_NAME" >/dev/null
fi

if ! container_exists; then
    echo "Creating container '$CONTAINER_NAME' ($IMAGE) on port $DB_PORT..."
    docker run --detach \
        --name "$CONTAINER_NAME" \
        --env POSTGRES_DB="$DB_NAME" \
        --env POSTGRES_USER="$DB_USERNAME" \
        --env POSTGRES_PASSWORD="$DB_PASSWORD" \
        --publish "$DB_PORT:5432" \
        --volume "$VOLUME_NAME:/var/lib/postgresql/data" \
        "$IMAGE" >/dev/null
elif [ "$(docker container inspect --format '{{.State.Running}}' "$CONTAINER_NAME")" != "true" ]; then
    echo "Starting existing container '$CONTAINER_NAME'..."
    docker start "$CONTAINER_NAME" >/dev/null
else
    echo "Container '$CONTAINER_NAME' is already running."
fi

echo -n "Waiting for PostgreSQL"
for _ in $(seq 1 30); do
    # TCP check: the image's first-run init server only listens on the Unix socket.
    if docker exec "$CONTAINER_NAME" pg_isready --quiet --host 127.0.0.1 --username "$DB_USERNAME" --dbname "$DB_NAME"; then
        echo
        echo "PostgreSQL is ready at localhost:$DB_PORT/$DB_NAME (user '$DB_USERNAME')."
        exit 0
    fi
    echo -n "."
    sleep 1
done

echo
echo "PostgreSQL did not become ready in 30 seconds. Check: docker logs $CONTAINER_NAME" >&2
exit 1
