#!/bin/bash -e

# Remove old reports
export INSTANCE="<!--|INSTANCE|-->"

if [ -e "<!--|ROOT_PATH|-->/server_apps/DB_maintenance/warehouse/chromosomeMartPostgres/runChromosomeMartReportPostgres.txt" ]; then
  /bin/rm "<!--|ROOT_PATH|-->/server_apps/DB_maintenance/warehouse/chromosomeMartPostgres/runChromosomeMartReportPostgres.txt"
fi

if [ -e "<!--|ROOT_PATH|-->/server_apps/DB_maintenance/warehouse/chromosomeMartPostgres/regenChromosomeMartReportPostgres.txt" ]; then
  /bin/rm "<!--|ROOT_PATH|-->/server_apps/DB_maintenance/warehouse/chromosomeMartPostgres/regenChromosomeMartReportPostgres.txt"
fi

if [ -e "<!--|SOURCEROOT|-->/reports/tests/chromosomeMartUnitTestsPostgres.txt" ]; then
  /bin/rm "<!--|SOURCEROOT|-->/reports/tests/chromosomeMartUnitTestsPostgres.txt"
fi

if [ -e "<!--|ROOT_PATH|-->/server_apps/DB_maintenance/warehouse/chromosomeMartPostgres/chromosomeMartUnitTestsPostgres.txt" ]; then
  /bin/rm "<!--|ROOT_PATH|-->/server_apps/DB_maintenance/warehouse/chromosomeMartPostgres/chromosomeMartUnitTestsPostgres.txt"
fi

echo "done with file delete"

# Build up the warehouse
<!--|ROOT_PATH|-->/server_apps/DB_maintenance/warehouse/chromosomeMartPostgres/runChromosomeMart.sh

if [ $? -ne 0 ]; then
  echo "regen chromosome mart (the building tables, not the public tables) failed"
  exit 1
fi

echo "done with runchromosomemart on <!--|DB_NAME|-->"

# Run the validation tests via Ant
cd "<!--|SOURCEROOT|-->" || exit 1
echo "cd'd to <!--|SOURCEROOT|-->"

ant run-chromosomemart-unittests &> reports/tests/chromosomeMartUnitTestsPostgres.txt
cp reports/tests/chromosomeMartUnitTestsPostgres.txt "<!--|TARGETROOT|-->/server_apps/DB_maintenance/warehouse/chromosomeMartPostgres/"

if [ $? -ne 0 ]; then
  echo "regen chromosome mart (the building tables, not the public tables) failed on unit tests"
  exit 1
fi

# Move the current table data to backup, move the new data to current
${PGBINDIR}/psql -v ON_ERROR_STOP=1 "<!--|DB_NAME|-->" < "<!--|ROOT_PATH|-->/server_apps/DB_maintenance/warehouse/chromosomeMartPostgres/chromosomeMartRegen.sql"

if [ $? -ne 0 ]; then
  echo "refresh chromosome mart (the public tables) failed and was rolled back"
  exit 1
fi

echo "success"
exit 0
