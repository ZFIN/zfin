#!/bin/bash

echo "Resetting elastic password."
ELASTIC_PASS=`docker compose exec elasticsearch bin/elasticsearch-reset-password --batch -s --user elastic > /run/secrets/elastic_pass`
echo "ELASTIC_PASSWORD=${ELASTIC_PASS}" > /run/secrets/elastic_pass.env

echo "Resetting logstash password."
LOGSTASH_PASS=`docker compose exec elasticsearch bin/elasticsearch-reset-password --batch -s --user logstash_internal > /run/secrets/logstash_pass`
echo "LOGSTASH_INTERNAL_PASSWORD=${LOGSTASH_PASS}" > /run/secrets/logstash_pass.env

echo "Resetting kibana password."
KIBANA_PASS=`docker compose exec elasticsearch bin/elasticsearch-reset-password --batch -s --user kibana_system`
echo "KIBANA_SYSTEM_PASSWORD=${KIBANA_PASS}" > /run/secrets/kibana_pass.env
