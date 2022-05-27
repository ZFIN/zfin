#!/bin/bash -e

${PGBINDIR}/psql $DBNAME < getEnsemblTscripts.sql
