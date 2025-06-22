#!/bin/bash

echo "Starting RawSource application on Railway..."
echo "PORT: $PORT (default: 3000)"
echo "SPRING_PROFILES_ACTIVE: railway"

# Set the port explicitly
export SERVER_PORT=$PORT

# Start the application with Railway profile
java -Dserver.port=$PORT \
     -Dspring.profiles.active=railway \
     -Dserver.ssl.enabled=false \
     -jar target/rawsource-0.0.1-SNAPSHOT.jar 