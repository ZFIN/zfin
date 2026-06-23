#!/bin/bash -e
# pipefail so a psql failure in a "psql ... | tee" pipeline still aborts the
# script -- tee always exits 0, which would otherwise mask the failure.
set -o pipefail

# rm old reports
date;

rm -f $ROOT_PATH/server_apps/DB_maintenance/warehouse/phenotypeMart/runPhenotypeMartReportPostgres.txt
rm -f $ROOT_PATH/server_apps/DB_maintenance/warehouse/phenotypeMart/regenPhenotypeMartReportPostgres.txt
rm -f $SOURCEROOT/reports/tests/phenotypeMartUnitTestsPostgres.txt
rm -f $ROOT_PATH/server_apps/DB_maintenance/warehouse/phenotypeMart/phenotypeMartUnitTestsPostgres.txt


echo "done with file delete" ;
# build up the warehouse: (re)build the *_temp staging tables from source. This
# is the slow part; it runs in its own transaction, committed before the apply
# below, so the build/apply staging boundary holds (a failed apply can retry
# without rebuilding). tee for console visibility; pipefail (top) unmasks a
# psql failure that tee's exit 0 would otherwise hide.
${PGBINDIR}/psql -v ON_ERROR_STOP=1 $DB_NAME -c 'SELECT regen_phenotype_mart_populate_temp_tables();' 2>&1 | tee "$ROOT_PATH/server_apps/DB_maintenance/warehouse/phenotypeMart/runPhenotypeMartReportPostgres.txt"

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


# apply the freshly-built *_temp tables onto the live tables, incrementally.

# tee so the apply's per-table insert/update/delete NOTICEs show on the Jenkins
# console as well as the report file. pipefail (above) keeps a psql failure from
# being masked by tee.
${PGBINDIR}/psql -v ON_ERROR_STOP=1 $DB_NAME < $ROOT_PATH/server_apps/DB_maintenance/warehouse/phenotypeMart/refreshPhenotypeMart.sql 2>&1 | tee $ROOT_PATH/server_apps/DB_maintenance/warehouse/phenotypeMart/regenPhenotypeMartReportPostgres.txt

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
