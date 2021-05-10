#! /bin/tcsh
### load_run_report_hit.sh
### is a wrapper around the sql to choose the database

if ("commit" =~ $1) then
	cat load_run_report_hit.sql commit.sql | ${PGBINDIR}/psql <!--|DB_NAME|-->
else
	cat load_run_report_hit.sql rollback.sql | ${PGBINDIR}/psql <!--|DB_NAME|-->
endif
