#!/bin/bash

URL="http://localhost:9400/metrics"
TARGET="azure_service_bus_sink_task_message_count_total 40000.0"

while true; do
	# Fetch the contents of the URL
	RESPONSE=$(curl -s "$URL")

	# Check if the response contains the target string
	if echo "$RESPONSE" | grep -q "$TARGET"; then
		echo "Target string found: $TARGET"
		break
	fi

	# Wait for a short time before retrying
	sleep 2
done
