#!/bin/bash

COMPOSE_FILE="docker-compose.yml"
VOLUME_NAME="qnr_postgres-data"
CONTAINERS=("postgres" "springboot-docker-container" "redis-cache-db")

echo "Stopping specified containers..."
for CONTAINER in "${CONTAINERS[@]}"; do
    docker stop $CONTAINER 2>/dev/null && docker rm $CONTAINER 2>/dev/null
done

echo "Removing specific volume: $VOLUME_NAME..."
docker volume rm $VOLUME_NAME 2>/dev/null || echo "Volume $VOLUME_NAME not found or already removed."

echo "Starting Docker Compose services..."
docker-compose -f $COMPOSE_FILE up -d

echo "Docker Compose services restarted successfully!"
