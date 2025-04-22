#!/bin/bash -e

# Remove old reports

rm -f "$ROOT_PATH/server_apps/DB_maintenance/warehouse/chromosomeMartPostgres/runChromosomeMartReportPostgres.txt"
rm -f "$ROOT_PATH/server_apps/DB_maintenance/warehouse/chromosomeMartPostgres/regenChromosomeMartReportPostgres.txt"
rm -f "$ROOT_PATH/reports/tests/chromosomeMartUnitTestsPostgres.txt"
rm -f "$ROOT_PATH/server_apps/DB_maintenance/warehouse/chromosomeMartPostgres/chromosomeMartUnitTestsPostgres.txt"

echo "done with file delete"

# Build up the warehouse
chmod +x "$ROOT_PATH/server_apps/DB_maintenance/warehouse/chromosomeMartPostgres/runChromosomeMart.sh"
"$ROOT_PATH/server_apps/DB_maintenance/warehouse/chromosomeMartPostgres/runChromosomeMart.sh"

if [ $? -ne 0 ]; then
  echo "regen chromosome mart (the building tables, not the public tables) failed"
  exit 1
fi

echo "done with runchromosomemart on $DB_NAME"

# Run the validation tests via Ant
cd "$SOURCEROOT" || exit 1
echo "cd'd to $SOURCEROOT"

mkdir -p reports/tests
ant run-chromosomemart-unittests &> reports/tests/chromosomeMartUnitTestsPostgres.txt
cp reports/tests/chromosomeMartUnitTestsPostgres.txt "$TARGETROOT/server_apps/DB_maintenance/warehouse/chromosomeMartPostgres/"

if [ $? -ne 0 ]; then
  echo "regen chromosome mart (the building tables, not the public tables) failed on unit tests"
  exit 1
fi

# Move the current table data to backup, move the new data to current
${PGBINDIR}/psql --echo-all -v ON_ERROR_STOP=1 "$DB_NAME" < "$ROOT_PATH/server_apps/DB_maintenance/warehouse/chromosomeMartPostgres/chromosomeMartRegen.sql" &> "$ROOT_PATH/server_apps/DB_maintenance/warehouse/chromosomeMartPostgres/runChromosomeMartReport.txt"

if [ $? -ne 0 ]; then
  echo "refresh chromosome mart (the public tables) failed and was rolled back"
  exit 1
fi

echo "success"
exit 0
