#!/bin/tcsh

# rm old reports

setenv INSTANCE <!--|INSTANCE|-->;

if ( -e <!--|ROOT_PATH|-->/server_apps/DB_maintenance/warehouse/expressionMart/runExpressionMartReport_PG.txt) then
 /bin/rm <!--|ROOT_PATH|-->/server_apps/DB_maintenance/warehouse/expressionMart/runExpressionMartReport_PG.txt

endif

if ( -e <!--|ROOT_PATH|-->/server_apps/DB_maintenance/warehouse/expressionMart/regenExpressionMartReport_PG.txt) then
 /bin/rm <!--|ROOT_PATH|-->/server_apps/DB_maintenance/warehouse/expressionMart/regenExpressionMartReport_PG.txt

endif

if ( -e <!--|SOURCEROOT|-->/reports/tests/expressionMartUnitTests_PG.txt) then
 /bin/rm <!--|SOURCEROOT|-->/reports/tests/expressionMartUnitTests_PG.txt
endif

if ( -e <!--|ROOT_PATH|-->/server_apps/DB_maintenance/warehouse/expressionMart/expressionMartUnitTests_PG.txt) then
 /bin/rm <!--|ROOT_PATH|-->/server_apps/DB_maintenance/warehouse/expressionMart/expressionMartUnitTests_PG.txt
endif


echo "done with file delete" ;
# build up the warehouse
<!--|ROOT_PATH|-->/server_apps/DB_maintenance/warehouse/expressionMart/runExpressionMart_PG.sh <!--|DB_NAME|--> 

if ($? != 0) then
 echo "regen expression mart (the building tables, not the public tables) failed on";
exit 1;
endif

echo "done with runexpressionmart on <!--|DB_NAME|-->";
# run the validation tests via ant.

cd <!--|SOURCEROOT|-->
echo "cd'd to <!--|SOURCEROOT|-->" ;

/private/bin/ant run-expressionmart-unittests >&! reports/tests/expressionMartUnitTests_PG.txt
cp reports/tests/expressionMartUnitTests_PG.txt <!--|TARGETROOT|-->/server_apps/DB_maintenance/warehouse/expressionMart/.

if ($? != 0) then
   echo "regen expression mart (the building tables, not the public tables) failed on unit tests";  
exit 1;
endif

#echo "done with ant tests" ;

# move the current table data to backup, move the new data to current.

${PGBINDIR}/psql <!--|DB_NAME|--> < <!--|ROOT_PATH|-->/server_apps/DB_maintenance/warehouse/expressionMart/expressionMartRegen_PG.sql

if ($? != 0) then
   echo "refresh expression mart (the public tables) failed and was rolled back";
exit 1;
endif

echo "select regen_expression_term_fast_search()" | ${PGBINDIR}/psql $DBNAME;

echo "select regen_feature_term_fast_search()" | ${PGBINDIR}/psql $DBNAME;

echo "success" ;

exit 0;
