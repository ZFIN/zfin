#!/bin/bash -e

# $1: Database name
CHROMOSOMEMARTDIR="$ROOT_PATH/server_apps/DB_maintenance/warehouse/chromosomeMartPostgres/"
FULL_SCRIPT_FILE="$CHROMOSOMEMARTDIR/chromosomeMartAutomated.sql"
CONVERT_CHROMOSOMEMART_FILE="$CHROMOSOMEMARTDIR/chromosomeMartRegen.sql"

rm -f "$FULL_SCRIPT_FILE" "$CONVERT_CHROMOSOMEMART_FILE"

#These all get combined into chromosomeMartAutomated.sql
chromosomeMartScripts=(
    "begin.sql"
    "schemaTables.sql"
    "commit.sql"
    "begin.sql"
    "populateTables.sql"
    "updateUniqueLocationTable.sql"
    "commit.sql"
)

#These all get combined into chromosomeMartRegen.sql
regenChromosomeMartScripts=(
    "begin.sql"
    "refreshChromosomeMart.sql"
    "commit.sql"
)

touch "$FULL_SCRIPT_FILE" "$CONVERT_CHROMOSOMEMART_FILE"

for name in "${chromosomeMartScripts[@]}"; do
    echo "$CHROMOSOMEMARTDIR$name"
    cat "$CHROMOSOMEMARTDIR/$name" >> "$FULL_SCRIPT_FILE"
done

cat "$ROOT_PATH/server_apps/DB_maintenance/warehouse/chromosomeMartPostgres/updateSequenceFeatureChromosomeLocationPostgres.sql" >> "$FULL_SCRIPT_FILE"

for name in "${regenChromosomeMartScripts[@]}"; do
    echo "$CHROMOSOMEMARTDIR$name"
    cat "$CHROMOSOMEMARTDIR/$name" >> "$CONVERT_CHROMOSOMEMART_FILE"
done

if [ -z "$1" ]; then
    echo "ready to start dropTables.sql DBNAME from environment."
    ${PGBINDIR}/psql -v ON_ERROR_STOP=1 "$DBNAME" < "$FULL_SCRIPT_FILE"
else
    echo "ready to start dropTables.sql DBNAME provided from script call."
    ${PGBINDIR}/psql -v ON_ERROR_STOP=1 "$1" < "$FULL_SCRIPT_FILE"
fi

exit 0