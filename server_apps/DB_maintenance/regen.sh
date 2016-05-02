#! /bin/tcsh -e 
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

echo "Starting regen_genox at `date`"
echo 'execute function regen_genox(); update statistics for procedure' | dbaccess <!--|DB_NAME|-->

echo "Starting regen_anatomy_counts at `date`"
echo 'execute function regen_anatomy_counts(); update statistics for procedure' | dbaccess <!--|DB_NAME|-->

echo "Starting regen_term at `date`"
echo 'execute function regen_term(); update statistics for procedure' | dbaccess <!--|DB_NAME|-->

echo "Starting regen_names at `date`"
echo 'execute function regen_names(); update statistics for procedure' | dbaccess <!--|DB_NAME|-->

echo "Starting regen_expression_term_fast_search at `date`"
echo 'execute function regen_expression_term_fast_search(); update statistics for procedure' | dbaccess <!--|DB_NAME|-->

echo "Starting regen_clean_expression at `date`"
echo 'execute function regen_clean_expression(); update statistics for procedure' | dbaccess <!--|DB_NAME|-->

echo "Starting regen_fish_Components at `date`"
echo 'execute function regen_fish_components(); update statistics for procedure' | dbaccess <!--|DB_NAME|-->

echo "Starting regen_pheno_fast_search at `date`"
dbaccess -a <!--|DB_NAME|--> <!--|TARGETROOT|-->/server_apps/DB_maintenance/pheno/pheno_term_regen.sql 

echo "do extra update statistics high to try and avoid 710 errors `date`"
echo 'update statistics high' | dbaccess <!--|DB_NAME|-->

echo "Finished at `date`"
