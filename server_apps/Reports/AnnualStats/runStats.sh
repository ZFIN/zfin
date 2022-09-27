#!/bin/bash -e

psql -v ON_ERROR_STOP=1 -d <!--|DB_NAME|--> -a -f stats.sql

