#!/bin/bash -e

${PGBINDIR}/psql <!--|DB_NAME|--> < <!--|ROOT_PATH|-->/server_apps/Reports/PubTracking/averageTimeInBinsCumulative_PG.sql
