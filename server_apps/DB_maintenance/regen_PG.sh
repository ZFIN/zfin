#! /bin/tcsh -e 
#--------------
# Run regen functions from cron.
# Update statistics for procedures after every invocation.  This avoids 
# certain informix errors after the procedure is run.  The procedures
# themselves update statistics for the tables they generate. 

echo "Starting regen_genox at `date`"
echo 'select regen_genox(); ' | ${PGBINDIR}/psql ${DBNAME}

echo "Starting regen_anatomy_counts at `date`"
echo 'execute function regen_anatomy_counts();' | ${PGBINDIR}/psql ${DBNAME}

echo "Starting regen_term at `date`"
echo 'execute function regen_term();' | ${PGBINDIR}/psql ${DBNAME}

echo "Starting regen_names at `date`"
echo 'execute function regen_names();' | ${PGBINDIR}/psql ${DBNAME}

echo "Starting regen_expression_term_fast_search at `date`"
echo 'execute function regen_expression_term_fast_search();' | ${PGBINDIR}/psql ${DBNAME}

echo "Starting regen_clean_expression at `date`"
echo 'execute function regen_clean_expression();' | ${PGBINDIR}/psql ${DBNAME}

echo "Starting regen_fish_Components at `date`"
echo 'execute function regen_fish_components();' | ${PGBINDIR}/psql ${DBNAME}

echo "Starting regen_pheno_fast_search at `date`"
${PGBINDIR}/psql ${DBNAME} < <!--|TARGETROOT|-->/server_apps/DB_maintenance/pheno/pheno_term_regen.sql 

echo "vacuum daily"
echo 'vacuum (analyze)' | ${PGBINDIR}/psql ${DBNAME}


echo "Finished at `date`"
