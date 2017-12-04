#!/bin/tcsh

# rm old reports

setenv INSTANCE <!--|INSTANCE|-->;

if ( -e <!--|ROOT_PATH|-->/server_apps/DB_maintenance/warehouse/chromosomeMartPostgres/runChromosomeMartReportPostgres.txt) then
 /bin/rm <!--|ROOT_PATH|-->/server_apps/DB_maintenance/warehouse/chromosomeMartPostgres/runChromosomeMartReportPostgres.txt

endif

if ( -e <!--|ROOT_PATH|-->/server_apps/DB_maintenance/warehouse/chromosomeMartPostgres/regenChromosomeMartReportPostgres.txt) then
 /bin/rm <!--|ROOT_PATH|-->/server_apps/DB_maintenance/warehouse/chromosomeMartPostgres/regenChromosomeMartReportPostgres.txt

endif

if ( -e <!--|SOURCEROOT|-->/reports/tests/chromosomeMartUnitTestsPostgres.txt) then
 /bin/rm <!--|SOURCEROOT|-->/reports/tests/chromosomeMartUnitTestsPostgres.txt
endif

if ( -e <!--|ROOT_PATH|-->/server_apps/DB_maintenance/warehouse/chromosomeMartPostgres/chromosomeMartUnitTestsPostgres.txt) then
 /bin/rm <!--|ROOT_PATH|-->/server_apps/DB_maintenance/warehouse/chromosomeMartPostgres/chromosomeMartUnitTests.txt

endif

echo "done with file delete" ;
# build up the warehouse
<!--|ROOT_PATH|-->/server_apps/DB_maintenance/warehouse/chromosomeMartPostgres/runChromosomeMart.sh 

if ($? != 0) then
 echo "regen chromosome mart (the building tables, not the public tables) failed";
exit 1;
endif

echo "done with runchromosomemart on <!--|DB_NAME|--> ";
# run the validation tests via ant.

cd <!--|SOURCEROOT|-->
echo "cd'd to <!--|SOURCEROOT|-->" ;

/private/bin/ant run-chromosomemart-unittests >&! reports/tests/chromosomeMartUnitTestsPostgres.txt
cp reports/tests/chromosomeMartUnitTestsPostgres.txt <!--|TARGETROOT|-->/server_apps/DB_maintenance/warehouse/chromosomeMartPostgres/.

if ($? != 0) then
   echo "regen chromosome mart (the building tables, not the public tables) failed on unit tests";
exit 1;
endif

#echo "done with ant tests" ;

# move the current table data to backup, move the new data to current.

${PGBINDIR}/psql <!--|DB_NAME|--> < <!--|ROOT_PATH|-->/server_apps/DB_maintenance/warehouse/chromosomeMartPostgres/chromosomeMartRegen.sql 

if ($? != 0) then
   echo "refresh chromosome mart (the public tables) failed and was rolled back";
exit 1;
endif

echo "success" ;

exit 0;
