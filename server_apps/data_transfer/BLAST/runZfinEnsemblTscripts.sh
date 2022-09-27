#!/bin/bash -e

${PGBINDIR}/psql -v ON_ERROR_STOP=1 $DBNAME < getEnsemblTscripts.sql
