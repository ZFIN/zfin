#!/bin/tcsh

set delete_command="delete from database_info where di_database_unloaded='${DBNAME}';"

echo $delete_command

echo $delete_command | ${PGBINDIR}/psql -v ON_ERROR_STOP=1 ${DBNAME}

set insert_command="insert into database_info (di_date_unloaded, di_database_unloaded) select CURRENT_TIMESTAMP, '${DBNAME}' from single;"

echo $insert_command |  ${PGBINDIR}/psql -v ON_ERROR_STOP=1 ${DBNAME}
