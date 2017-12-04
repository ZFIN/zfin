#!/bin/tcsh

# rm old reports
date;
setenv INSTANCE <!--|INSTANCE|-->;

if ( -e <!--|ROOT_PATH|-->/server_apps/DB_maintenance/warehouse/phenotypeMart/runPhenotypeMartReportPostgres_PG.txt) then
 /bin/rm <!--|ROOT_PATH|-->/server_apps/DB_maintenance/warehouse/phenotypeMart/runPhenotypeMartReportPostgres_PG.txt

endif

if ( -e <!--|ROOT_PATH|-->/server_apps/DB_maintenance/warehouse/phenotypeMart/regenPhenotypeMartReportPostgres_PG.txt) then
 /bin/rm <!--|ROOT_PATH|-->/server_apps/DB_maintenance/warehouse/phenotypeMart/regenPhenotypeMartReportPostgres_PG.txt

endif

if ( -e <!--|SOURCEROOT|-->/reports/tests/phenotypeMartUnitTestsPostgres_PG.txt) then
 /bin/rm <!--|SOURCEROOT|-->/reports/tests/phenotypeMartUnitTestsPostgres_PG.txt
endif

if ( -e <!--|ROOT_PATH|-->/server_apps/DB_maintenance/warehouse/phenotypeMart/phenotypeMartUnitTestsPostgres_PG.txt) then
 /bin/rm <!--|ROOT_PATH|-->/server_apps/DB_maintenance/warehouse/phenotypeMart/phenotypeMartUnitTestsPostgres_PG.txt
endif


echo "done with file delete" ;
# build up the warehouse
<!--|ROOT_PATH|-->/server_apps/DB_maintenance/warehouse/phenotypeMart/runPhenotypeMart_PG.sh <!--|DB_NAME|--> >&! <!--|ROOT_PATH|-->/server_apps/DB_maintenance/warehouse/phenotypeMart/runPhenotypeMartReportPostgres_PG.txt

if ($? != 0) then
 echo "regen phenotype mart (the building tables, not the public tables) failed on";
exit 1;
endif

echo "done with runphenotypemart on temp tables <!--|DB_NAME|-->";
# run the validation tests via ant.
date;
cd <!--|SOURCEROOT|-->
echo "cd'd to <!--|SOURCEROOT|-->" ;

/private/bin/ant run-phenotypemart-unittests >&! reports/tests/phenotypeMartUnitTests_PG.txt
cp reports/tests/phenotypeMartUnitTests_PG.txt <!--|TARGETROOT|-->/server_apps/DB_maintenance/warehouse/phenotypeMart/.

if ($? != 0) then
   echo "regen phenotype mart (the building tables, not the public tables) failed on unit tests";  
exit 1;
endif

date;
echo "done with phenotype mart building public" ;


# move the current table data to backup, move the new data to current.

${PGBINDIR}/psql <!--|DB_NAME|--> < <!--|ROOT_PATH|-->/server_apps/DB_maintenance/warehouse/phenotypeMart/phenotypeMartRegen_PG.sql >&! <!--|ROOT_PATH|-->/server_apps/DB_maintenance/warehouse/phenotypeMart/regenPhenotypeMartReportPostgres_PG.txt

if ($? != 0) then
   echo "refresh phenotype mart (the public tables) failed and was rolled back";
exit 1;
endif

echo "done regen public phenotype tables" ;
date;

echo "start regen_genox()";

echo "select regen_genox();" | ${PGBINDIR}/psql $DBNAME;

date;
echo "done with regen_genox()";


echo "start regen_pheno_term_regen()";
${PGBINDIR}/psql $DBNAME < $TARGETROOT/server_apps/DB_maintenance/pheno/pheno_term_regen_PG.sql
date;
echo "done with pheno_term_regen()";




echo "success" ;

exit 0;
