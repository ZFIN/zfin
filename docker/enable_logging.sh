#!/bin/bash

cd -- "$( dirname -- "${BASH_SOURCE[0]}" )"

if [[ "${ELASTIC_PASSWORD}" == "ChangeMe!" ]]; then
        echo  'Cannot use "ChangeMe!" as a password. Please change.'
	exit
fi

docker compose down kibana filebeat elasticsearch

docker compose up --detach elasticsearch 

./elasticsearch/configure_users.sh

docker compose up --detach filebeat 

docker compose exec filebeat filebeat setup -e

docker compose up --detach kibana 
