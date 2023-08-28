#!/bin/tcsh

echo "start regen_genofig_finish_cleanup()";
echo "select regen_genofig_finish_cleanup();" | ${PGBINDIR}/psql -v ON_ERROR_STOP=1 $DBNAME;
if ($? != 0) then
   echo "regen_genofig_finish_cleanup failed";
exit 1;
endif

date;
echo "done with regen_genofig_finish_cleanup()";
