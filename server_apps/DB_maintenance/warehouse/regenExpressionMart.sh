#!/bin/tcsh

# rm old reports

setenv INSTANCE <!--|INSTANCE|-->;

if ( -e <!--|ROOT_PATH|-->/server_apps/DB_maintenance/warehouse/expressionMart/runExpressionMartReport.txt) then
 /bin/rm <!--|ROOT_PATH|-->/server_apps/DB_maintenance/warehouse/expressionMart/runExpressionMartReport.txt

endif

if ( -e <!--|ROOT_PATH|-->/server_apps/DB_maintenance/warehouse/expressionMart/regenExpressionMartReport.txt) then
 /bin/rm <!--|ROOT_PATH|-->/server_apps/DB_maintenance/warehouse/expressionMart/regenExpressionMartReport.txt

endif

if ( -e <!--|SOURCEROOT|-->/reports/tests/expressionMartUnitTests.txt) then
 /bin/rm <!--|SOURCEROOT|-->/reports/tests/expressionMartUnitTests.txt
endif

if ( -e <!--|ROOT_PATH|-->/server_apps/DB_maintenance/warehouse/expressionMart/expressionMartUnitTests.txt) then
 /bin/rm <!--|ROOT_PATH|-->/server_apps/DB_maintenance/warehouse/expressionMart/expressionMartUnitTests.txt
endif


echo "done with file delete" ;
# build up the warehouse
<!--|ROOT_PATH|-->/server_apps/DB_maintenance/warehouse/expressionMart/runExpressionMart.sh <!--|DB_NAME|-->

if ($? != 0) then
 echo "regen expression mart (the building tables, not the public tables) failed on";
exit 1;
endif

echo "done with runexpressionmart on <!--|DB_NAME|-->";
# run the validation tests via ant.

cd <!--|SOURCEROOT|-->
echo "cd'd to <!--|SOURCEROOT|-->" ;

# run-expressionmart-unittests ant target no longer exists?
# /opt/zfin/bin/ant run-expressionmart-unittests >&! reports/tests/expressionMartUnitTests.txt
# cp reports/tests/expressionMartUnitTests.txt <!--|TARGETROOT|-->/server_apps/DB_maintenance/warehouse/expressionMart/.

if ($? != 0) then
   echo "regen expression mart (the building tables, not the public tables) failed on unit tests";  
exit 1;
endif

#echo "done with ant tests" ;

# move the current table data to backup, move the new data to current.

${PGBINDIR}/psql -v ON_ERROR_STOP=1 <!--|DB_NAME|--> < <!--|ROOT_PATH|-->/server_apps/DB_maintenance/warehouse/expressionMart/expressionMartRegen.sql

if ($? != 0) then
   echo "refresh expression mart (the public tables) failed and was rolled back";
exit 1;
endif

echo "select regen_expression_term_fast_search()" | ${PGBINDIR}/psql -v ON_ERROR_STOP=1 $DBNAME;

echo "select regen_feature_term_fast_search()" | ${PGBINDIR}/psql -v ON_ERROR_STOP=1 $DBNAME;

echo "success" ;

exit 0;
