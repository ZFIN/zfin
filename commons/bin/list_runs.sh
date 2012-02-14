#! /bin/tcsh

echo " select run_zdb_id[1,25]zdb,run_name name,run_type[1,12] from run order by run_date" | dbaccess -a $DBNAME
