#! /bin/tcsh
#--------------
# Run regen functions from cron.
# Update statistics for procedures after every invocation.  This avoids 
# certain informix errors after the procedure is run.  The procedures
# themselves update statistics for the tables they generate. 

setenv INFORMIXDIR <!--|INFORMIX_DIR|-->
setenv INFORMIXSERVER <!--|INFORMIX_SERVER|-->
setenv ONCONFIG <!--|ONCONFIG_FILE|-->
setenv INFORMIXSQLHOSTS ${INFORMIXDIR}/etc/<!--|SQLHOSTS_FILE|-->
setenv LD_LIBRARY_PATH ${INFORMIXDIR}/lib:${INFORMIXDIR}/lib/esql
setenv PATH <!--|INFORMIX_DIR|-->/bin:$PATH

echo "Starting regen_anatomy at `date`"
echo 'execute function regen_anatomy(); update statistics for procedure' | dbaccess <!--|DB_NAME|-->

echo "Starting regen_maps at `date`"
echo 'execute function regen_maps(); update statistics for procedure' | dbaccess <!--|DB_NAME|-->

echo "Starting regen_names at `date`"
echo 'execute function regen_names(); update statistics for procedure' | dbaccess <!--|DB_NAME|-->

echo "Starting regen_oevdisp at `date`"
echo 'execute function regen_oevdisp(); update statistics for procedure' | dbaccess <!--|DB_NAME|-->

echo "Starting regen_feature_ao_fast_search at `date`"
echo 'execute function regen_feature_ao_fast_search(); update statistics for procedure' | dbaccess <!--|DB_NAME|-->

echo "do extra update statistics high to try and avoid 710 errors `date`"
echo 'update statistics high' | dbaccess <!--|DB_NAME|-->

echo "Finished at `date`"
