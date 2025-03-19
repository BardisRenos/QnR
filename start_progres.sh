#!/bin/bash

COMPOSE_FILE="docker-compose.yml"
SERVICE_NAME="postgres"

echo "Starting the PostgresSQL service..."
docker-compose -f $COMPOSE_FILE up -d $SERVICE_NAME

echo "PostgresSQL service started successfully!"
