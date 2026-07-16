#!/bin/bash
#
# Ad-hoc snapshot of one organization's marker_go_term_evidence rows to a CSV, at
# any time. Same data/logic the load jobs capture, but runnable on demand.
#
# TODO(ZFIN-8948): this snapshot + the logical diff (diff_mgte.sh/.sql) were built
# to investigate id/protein churn. Going forward only the DB-level csvDiff of two
# snapshots is needed, so this custom snapshot/logical-diff machinery is expected
# to be removed once the csvDiff-of-snapshots flow is settled.
#
# Usage:
#   snapshot_mgte.sh <output.csv> [org]      # org defaults to GOA (e.g. Noctua)
#
# Env: PGHOST (default: db), DBNAME (required) — same as the load jobs.
# Compare two snapshots at the database level with csvDiff (key = every column
# except zdb_id, ignore zdb_id).
#
set -euo pipefail

OUTFILE="${1:?usage: snapshot_mgte.sh <output.csv> [org]}"
ORG="${2:-GOA}"
DIR="$(cd "$(dirname "$0")" && pwd)"
STAGE="tmp_gaf_mgoe_snapshot_$$"   # per-process name so concurrent snapshots don't collide
PSQL="psql -v ON_ERROR_STOP=1 -h ${PGHOST:-db} -d ${DBNAME:?DBNAME must be set}"

$PSQL -v org="$ORG" -v stage="$STAGE" -f "$DIR/snapshot_mgte.sql"
$PSQL -c "\copy (SELECT * FROM $STAGE ORDER BY zdb_id) TO STDOUT CSV HEADER" > "$OUTFILE"
$PSQL -c "DROP TABLE IF EXISTS $STAGE" >/dev/null

echo "Wrote $(( $(wc -l < "$OUTFILE") - 1 )) rows to $OUTFILE"
