#!/bin/bash

echo "=== RawSource Railway Startup ==="
echo "Current directory: $(pwd)"
echo "PORT: $PORT (Railway assigned port)"
echo "SPRING_PROFILES_ACTIVE: railway"
echo "JAVA_HOME: $JAVA_HOME"
echo "Java version: $(java -version 2>&1 | head -1)"

# List files to verify the JAR exists
echo "Checking for JAR file..."
ls -la target/rawsource-*.jar

# Set the port explicitly to what Railway assigns
export SERVER_PORT=$PORT
echo "SERVER_PORT set to: $SERVER_PORT"

echo "Starting application on port $PORT..."
# Start the application with Railway profile and the correct port
java -Dserver.port=$PORT \
     -Dspring.profiles.active=railway \
     -Dserver.ssl.enabled=false \
     -Dlogging.level.com.example.rawsource=DEBUG \
     -jar target/rawsource-0.0.1-SNAPSHOT.jar 