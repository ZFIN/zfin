#!/bin/bash
# Loads the APM traces index template into Elasticsearch before starting apm-server.
# This ensures that traces-apm* indices get proper keyword mappings instead of
# Elasticsearch's default dynamic text+keyword mapping (which breaks APM UI aggregations).

set -e

ES_URL="http://elasticsearch:9200"
TEMPLATE_FILE="/usr/share/apm-server/traces-apm-template.json"
TEMPLATE_NAME="traces-apm-custom"

echo "Waiting for Elasticsearch to be ready..."
until curl -s -u "elastic:${ELASTIC_PASSWORD}" "${ES_URL}/_cluster/health" | grep -qE '"status":"(green|yellow)"'; do
  sleep 2
done
echo "Elasticsearch is ready."

echo "Loading index template '${TEMPLATE_NAME}'..."
HTTP_CODE=$(curl -s -o /dev/null -w '%{http_code}' \
  -X PUT "${ES_URL}/_index_template/${TEMPLATE_NAME}" \
  -u "elastic:${ELASTIC_PASSWORD}" \
  -H 'Content-Type: application/json' \
  -d @"${TEMPLATE_FILE}")

if [ "$HTTP_CODE" = "200" ]; then
  echo "Index template '${TEMPLATE_NAME}' loaded successfully."
else
  echo "WARNING: Failed to load index template (HTTP ${HTTP_CODE}). APM UI aggregations may not work correctly."
fi

# Hand off to the original entrypoint
exec /usr/local/bin/docker-entrypoint "$@"
