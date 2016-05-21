#!/bin/bash

JAVA_OPTS="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005 -Djava.security.egd=file:/dev/./urandom"

if [[ "${MODE}" = "backend" ]]; then
    echo "Starting backend..."
    java "${JAVA_OPTS}" -jar ./rso*-backend.jar
elif [[ "${MODE}" = "frontend" ]]; then
    if [[ "x${BACKEND_ENDPOINTS}" = "x" ]]; then
        echo "COUNTER_INCREMENT not set!"
        exit 1
    fi
    echo "Starting frontend (BACKEND_ENDPOINTS: ${BACKEND_ENDPOINTS}"
    java "${JAVA_OPTS}" -DbackendEndpoints="${BACKEND_ENDPOINTS}" -jar ./rso*-frontend.jar
else
    echo "Invalid mode: ${MODE}"
    exit 1
fi
