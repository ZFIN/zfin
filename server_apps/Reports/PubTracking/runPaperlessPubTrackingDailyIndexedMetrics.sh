#!/bin/bash -e

${PGBINDIR}/psql -v ON_ERROR_STOP=1 <!--|DB_NAME|--> < <!--|ROOT_PATH|-->/server_apps/Reports/PubTracking/paperlessPubTrackingDailyIndexedStats.sql
