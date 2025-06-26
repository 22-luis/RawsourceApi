#!/bin/bash

echo "=== Verifying Railway Environment Variables ==="

# Check if required Railway variables are set
if [ -z "$SPRING_DATASOURCE_URL" ]; then
    echo "ERROR: SPRING_DATASOURCE_URL is not set!"
    echo "Please configure the database connection in Railway."
    exit 1
fi

if [ -z "$SPRING_DATASOURCE_USERNAME" ]; then
    echo "ERROR: SPRING_DATASOURCE_USERNAME is not set!"
    echo "Please configure the database username in Railway."
    exit 1
fi

if [ -z "$SPRING_DATASOURCE_PASSWORD" ]; then
    echo "ERROR: SPRING_DATASOURCE_PASSWORD is not set!"
    echo "Please configure the database password in Railway."
    exit 1
fi

echo "✅ All required environment variables are set:"
echo "   SPRING_DATASOURCE_URL: $SPRING_DATASOURCE_URL"
echo "   SPRING_DATASOURCE_USERNAME: $SPRING_DATASOURCE_USERNAME"
echo "   SPRING_DATASOURCE_PASSWORD: [HIDDEN]"
echo "   PORT: $PORT"
echo "================================================" 