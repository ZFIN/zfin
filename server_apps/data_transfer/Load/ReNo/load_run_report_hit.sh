#! /bin/tcsh
### load_run_report_hit.sh
### is a wrapper around the sql to choose the database

### informix and database variables must be set

setenv INFORMIXDIR <!--|INFORMIX_DIR|-->
setenv INFORMIXSERVER <!--|INFORMIX_SERVER|-->
setenv ONCONFIG <!--|ONCONFIG_FILE|-->
setenv INFORMIXSQLHOSTS <!--|INFORMIX_DIR|-->/etc/<!--|SQLHOSTS_FILE|-->

###
if ("commit" =~ $1) then
	cat load_run_report_hit.sql commit.sql | <!--|INFORMIX_DIR|-->/bin/dbaccess -a <!--|DB_NAME|-->
else
	cat load_run_report_hit.sql rollback.sql | <!--|INFORMIX_DIR|-->/bin/dbaccess -a <!--|DB_NAME|-->
endif
