#!/bin/bash

cd -- "$( dirname -- "${BASH_SOURCE[0]}" )"

docker compose --profile logging up elasticsearch &

./elasticsearch/configure_users.sh

docker compose --profile logging up filebeat &

docker compose exec filebeat filebeat setup -e

docker compose --profile logging up kibana &
