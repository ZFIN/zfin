#!/bin/bash -e

psql -d <!--|DB_NAME|--> -a -f stats_PG.sql

