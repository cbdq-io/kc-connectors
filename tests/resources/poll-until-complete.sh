#!/bin/bash

URL="http://localhost:8000/metrics"
TARGET="azureservicebussink_message_count_total 1001.0"

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
