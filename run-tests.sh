#!/bin/bash

set -e

echo "Run postgres and mongo in docker"
docker-compose -f docker-compose-local.yaml up -d

sleep 5

echo "Build and run tests"
./gradlew clean build

echo "Stop and remove container"
docker-compose -f  docker-compose-local.yaml down -v

echo "Finish tests running!"
