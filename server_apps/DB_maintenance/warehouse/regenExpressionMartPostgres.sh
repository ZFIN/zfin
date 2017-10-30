#!/bin/tcsh

# rm old reports

setenv INSTANCE <!--|INSTANCE|-->;

if ( -e <!--|ROOT_PATH|-->/server_apps/DB_maintenance/warehouse/expressionMartPostgres/runExpressionMartReportPostgres.txt) then
 /bin/rm <!--|ROOT_PATH|-->/server_apps/DB_maintenance/warehouse/expressionMartPostgres/runExpressionMartReportPostgres.txt

endif

if ( -e <!--|ROOT_PATH|-->/server_apps/DB_maintenance/warehouse/expressionMartPostgres/regenExpressionMartReportPostgres.txt) then
 /bin/rm <!--|ROOT_PATH|-->/server_apps/DB_maintenance/warehouse/expressionMartPostgres/regenExpressionMartReportPostgres.txt

endif

if ( -e <!--|SOURCEROOT|-->/reports/tests/expressionMartUnitTestsPostgres.txt) then
 /bin/rm <!--|SOURCEROOT|-->/reports/tests/expressionMartUnitTestsPostgres.txt
endif

if ( -e <!--|ROOT_PATH|-->/server_apps/DB_maintenance/warehouse/expressionMartPostgres/expressionMartUnitTestsPostgres.txt) then
 /bin/rm <!--|ROOT_PATH|-->/server_apps/DB_maintenance/warehouse/expressionMartPostgres/expressionMartUnitTestsPostgres.txt
endif


echo "done with file delete" ;
# build up the warehouse
<!--|ROOT_PATH|-->/server_apps/DB_maintenance/warehouse/expressionMartPostgres/runExpressionMartPostgres.sh <!--|DB_NAME|--> >&! <!--|ROOT_PATH|-->/server_apps/DB_maintenance/warehouse/expressionMartPostgres/runExpressionMartReportPostgres.txt

if ($? != 0) then
 echo "regen expression mart (the building tables, not the public tables) failed on";
exit 1;
endif

echo "done with runexpressionmart on <!--|DB_NAME|-->";
# run the validation tests via ant.

cd <!--|SOURCEROOT|-->
echo "cd'd to <!--|SOURCEROOT|-->" ;

/private/bin/ant run-expressionmart-unittests >&! reports/tests/expressionMartUnitTestsPostgres.txt
cp reports/tests/expressionMartUnitTestsPostgres.txt <!--|TARGETROOT|-->/server_apps/DB_maintenance/warehouse/expressionMartPostgres/.

if ($? != 0) then
   echo "regen expression mart (the building tables, not the public tables) failed on unit tests";  
exit 1;
endif

#echo "done with ant tests" ;

# move the current table data to backup, move the new data to current.

${PGBINDIR}/psql <!--|DB_NAME|--> < <!--|ROOT_PATH|-->/server_apps/DB_maintenance/warehouse/expressionMartPostgres/expressionMartRegenPostgres.sql >&! <!--|ROOT_PATH|-->/server_apps/DB_maintenance/warehouse/expressionMartPostgres/regenExpressionMartReportPostgres.txt

if ($? != 0) then
   echo "refresh expression mart (the public tables) failed and was rolled back";
exit 1;
endif

echo "select regen_expression_term_fast_search()" | ${PGBINDIR} $DBNAME;

echo "select regen_feature_term_fast_search()" | ${PGBINDIR} $DBNAME;

echo "success" ;

exit 0;
