#!/bin/bash -e

# rm old reports
rm -f "$ROOT_PATH/server_apps/DB_maintenance/warehouse/expressionMart/runExpressionMartReport.txt"
rm -f "$ROOT_PATH/server_apps/DB_maintenance/warehouse/expressionMart/regenExpressionMartReport.txt"
rm -f "$SOURCEROOT/reports/tests/expressionMartUnitTests.txt"
rm -f "$ROOT_PATH/server_apps/DB_maintenance/warehouse/expressionMart/expressionMartUnitTests.txt"


echo "done with file delete" ;
# build up the warehouse
chmod +x "$ROOT_PATH/server_apps/DB_maintenance/warehouse/expressionMart/runExpressionMart.sh"
"$ROOT_PATH/server_apps/DB_maintenance/warehouse/expressionMart/runExpressionMart.sh" $DB_NAME

if [ $? -ne 0 ]; then
 echo "regen expression mart (the building tables, not the public tables) failed on";
 exit 1;
fi

echo "done with runexpressionmart on $DB_NAME";
# run the validation tests via ant.

cd "$SOURCEROOT"
echo "cd'd to $SOURCEROOT" ;

# run-expressionmart-unittests ant target no longer exists?
# /opt/zfin/bin/ant run-expressionmart-unittests >&! reports/tests/expressionMartUnitTests.txt
# cp reports/tests/expressionMartUnitTests.txt $TARGETROOT/server_apps/DB_maintenance/warehouse/expressionMart/.

if [ $? -ne 0 ]; then
 echo "regen expression mart (the building tables, not the public tables) failed on unit tests";
 exit 1;
fi

#echo "done with ant tests" ;


echo "select regen_expression_1term_fast_search()" | ${PGBINDIR}/psql -v ON_ERROR_STOP=1 $DBNAME;
echo "select regen_feature_term_fast_search()" | ${PGBINDIR}/psql -v ON_ERROR_STOP=1 $DBNAME;
echo "success" ;

exit 0;
