#!/bin/bash

COMPOSE_FILE="docker-compose.yml"

echo "Stopping all services..."
docker-compose -f $COMPOSE_FILE down

echo "All services have been stopped!"
