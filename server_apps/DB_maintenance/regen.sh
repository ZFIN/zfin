#! /bin/tcsh
#--------------
# Run regen functions from cron.  Currently only runs regen_maps.

setenv INFORMIXDIR <!--|INFORMIX_DIR|-->
setenv INFORMIXSERVER <!--|INFORMIX_SERVER|-->
setenv ONCONFIG <!--|ONCONFIG_FILE|-->
setenv INFORMIXSQLHOSTS ${INFORMIXDIR}/etc/<!--|SQLHOSTS_FILE|-->
setenv LD_LIBRARY_PATH ${INFORMIXDIR}/lib:${INFORMIXDIR}/lib/esql
setenv PATH <!--|INFORMIX_DIR|-->/bin:$PATH

echo "Starting regen_maps,genomics,anatomy,oevdisp,panel,fishsearch at `date`"
echo 'execute function regen_maps()' | dbaccess <!--|DB_NAME|-->
echo 'execute function regen_genomics()' | dbaccess <!--|DB_NAME|-->
echo 'execute function regen_oevdisp()' | dbaccess <!--|DB_NAME|-->
echo 'execute function regen_fishsearch()' | dbaccess <!--|DB_NAME|-->
echo "Finished at `date`"
