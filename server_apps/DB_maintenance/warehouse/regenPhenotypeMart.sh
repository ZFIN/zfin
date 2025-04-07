#!/bin/bash -e

# rm old reports
date;

rm -f $ROOT_PATH/server_apps/DB_maintenance/warehouse/phenotypeMart/runPhenotypeMartReportPostgres.txt
rm -f $ROOT_PATH/server_apps/DB_maintenance/warehouse/phenotypeMart/regenPhenotypeMartReportPostgres.txt
rm -f $SOURCEROOT/reports/tests/phenotypeMartUnitTestsPostgres.txt
rm -f $ROOT_PATH/server_apps/DB_maintenance/warehouse/phenotypeMart/phenotypeMartUnitTestsPostgres.txt


echo "done with file delete" ;
# build up the warehouse
chmod +x "$ROOT_PATH/server_apps/DB_maintenance/warehouse/phenotypeMart/runPhenotypeMart.sh"
"$ROOT_PATH/server_apps/DB_maintenance/warehouse/phenotypeMart/runPhenotypeMart.sh" $DB_NAME &> "$ROOT_PATH/server_apps/DB_maintenance/warehouse/phenotypeMart/runPhenotypeMartReportPostgres.txt"

if [ $? -ne 0 ]; then
 echo "regen phenotype mart (the building tables, not the public tables) failed on";
 exit 1;
fi

echo "done with runphenotypemart on temp tables $DB_NAME";
# run the validation tests via ant.
date;
cd $SOURCEROOT
echo "cd'd to $SOURCEROOT" ;

# There is no longer any 'run-phenotypemart-unittests' ant target?
#/opt/zfin/bin/ant run-phenotypemart-unittests >&! reports/tests/phenotypeMartUnitTests.txt
#cp reports/tests/phenotypeMartUnitTests.txt $TARGETROOT/server_apps/DB_maintenance/warehouse/phenotypeMart/.

if [ $? -ne 0 ]; then
 echo "regen phenotype mart (the building tables, not the public tables) failed on unit tests";
 exit 1;
fi

date;
echo "done with phenotype mart building public" ;


# move the current table data to backup, move the new data to current.

${PGBINDIR}/psql -v ON_ERROR_STOP=1 $DB_NAME < $ROOT_PATH/server_apps/DB_maintenance/warehouse/phenotypeMart/phenotypeMartRegen.sql &> $ROOT_PATH/server_apps/DB_maintenance/warehouse/phenotypeMart/regenPhenotypeMartReportPostgres.txt

if [ $? -ne 0 ]; then
 echo "refresh phenotype mart (the public tables) failed and was rolled back";
 exit 1;
fi

echo "done regen public phenotype tables" ;
date;

echo "start regen_genox()";
echo "select regen_genox();" | ${PGBINDIR}/psql -v ON_ERROR_STOP=1 $DBNAME;
if [ $? -ne 0 ]; then
 echo "regen phenotype mart failed";
 exit 1;
fi

date;
echo "done with regen_genox()";


echo "start regen_anatomy_counts()";
echo "select regen_anatomy_counts()" | ${PGBINDIR}/psql -v ON_ERROR_STOP=1 $DBNAME
if [ $? -ne 0 ]; then
 echo "regen phenotype mart failed";
 exit 1;
fi

date;
echo "done with regen_anatomy_counts()";

echo "start regen_pheno_term_regen()";
${PGBINDIR}/psql -v ON_ERROR_STOP=1 $DBNAME < $TARGETROOT/server_apps/DB_maintenance/pheno/pheno_term_regen.sql
if [ $? -ne 0 ]; then
 echo "regen phenotype mart failed";
 exit 1;
fi

date;
echo "done with pheno_term_regen()";

echo "start gradle runPhenotypeIndexer";
cd $SOURCEROOT
gradle runPhenotypeIndexer
if [ $? -ne 0 ]; then
 echo "gradle runPhenotypeIndexer failed";
 exit 1;
fi

date;
echo "done with gradle runPhenotypeIndexer";

echo "success" ;

exit 0;
