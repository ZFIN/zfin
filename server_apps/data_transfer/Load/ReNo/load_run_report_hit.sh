#! /bin/sh
### load_run_report_hit.sh
### is a wrapper around the sql to choose the database

### informix and database variables must be set

setenv INFORMIXDIR <!--|INFORMIX_DIR|-->
setenv INFORMIXSERVER <!--|INFORMIX_SERVER|-->
setenv ONCONFIG <!--|ONCONFIG_FILE|-->
setenv INFORMIXSQLHOSTS <!--|INFORMIX_DIR|-->/etc/<!--|SQLHOSTS_FILE|-->

###
<!--|INFORMIX_DIR|-->/bin/dbaccess  <!--|DB_NAME|-->  load_run_report_hit.sql
