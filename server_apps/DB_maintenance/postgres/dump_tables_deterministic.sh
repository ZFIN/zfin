#!/bin/bash
#
# Dump each table's data to its own file, ordered deterministically.
#
# Output: <OUTDIR>/<schema>.<table>.tsv  (TSV, no header, NULL = \N — psql \copy default)
# Ordering: by primary key if present; otherwise by every column. This makes the
# output byte-stable across runs against the same data, so files diff cleanly.
#
# Env:
#   DBNAME    target database (required)
#   PGBINDIR  path to psql (optional, defaults to PATH)
#   PGHOST    host (optional, defaults to libpq default)
#   OUTDIR    output directory (default: ./table_dumps)
#   SCHEMAS   comma-separated schema allowlist (default: all user schemas)
#   EXCLUDE   regex of fully-qualified table names to skip (default: none)
#
# Example:
#   DBNAME=zfin SCHEMAS=public OUTDIR=/tmp/d1 ./dump_tables_deterministic.sh

set -euo pipefail

: "${DBNAME:?DBNAME must be set}"
PSQL="${PGBINDIR:+$PGBINDIR/}psql"
OUTDIR="${OUTDIR:-./table_dumps}"
SCHEMAS="${SCHEMAS:-}"
EXCLUDE="${EXCLUDE:-}"

mkdir -p "$OUTDIR"

# Build schema filter clause for the table-listing query.
if [[ -n "$SCHEMAS" ]]; then
    schema_in=$(echo "$SCHEMAS" | sed "s/[^,]*/'&'/g")
    schema_filter="AND n.nspname IN ($schema_in)"
else
    schema_filter="AND n.nspname NOT IN ('pg_catalog','information_schema')
                   AND n.nspname NOT LIKE 'pg_%'"
fi

# List all base tables we should dump.
tables=()
while IFS= read -r line; do
    tables+=("$line")
done < <(
    "$PSQL" -d "$DBNAME" -At -F$'\t' <<SQL
SELECT n.nspname, c.relname
FROM pg_class c
JOIN pg_namespace n ON n.oid = c.relnamespace
WHERE c.relkind = 'r'
  $schema_filter
ORDER BY 1, 2;
SQL
)

echo "Dumping ${#tables[@]} tables to $OUTDIR"

for row in "${tables[@]}"; do
    schema="${row%%$'\t'*}"
    table="${row##*$'\t'}"
    fqn="${schema}.${table}"

    if [[ -n "$EXCLUDE" && "$fqn" =~ $EXCLUDE ]]; then
        echo "skip   $fqn"
        continue
    fi

    # Get the ordering column list: PK columns if a PK exists, else every column.
    order_cols=$("$PSQL" -d "$DBNAME" -At <<SQL
WITH pk AS (
    SELECT a.attname
    FROM pg_index i
    JOIN pg_attribute a
      ON a.attrelid = i.indrelid AND a.attnum = ANY(i.indkey)
    WHERE i.indrelid = '${schema}.${table}'::regclass
      AND i.indisprimary
    ORDER BY array_position(i.indkey, a.attnum)
), allcols AS (
    SELECT attname
    FROM pg_attribute
    WHERE attrelid = '${schema}.${table}'::regclass
      AND attnum > 0
      AND NOT attisdropped
    ORDER BY attnum
)
SELECT string_agg(quote_ident(attname), ', ')
FROM (SELECT attname FROM pk
      UNION ALL
      SELECT attname FROM allcols WHERE NOT EXISTS (SELECT 1 FROM pk)) t;
SQL
)

    out="$OUTDIR/${schema}.${table}.tsv"
    echo "dump   $fqn  (ORDER BY $order_cols)"
    "$PSQL" -d "$DBNAME" -v ON_ERROR_STOP=1 -c \
        "\copy (SELECT * FROM ${schema}.${table} ORDER BY ${order_cols}) TO '${out}'"
done

echo "done. ${#tables[@]} files in $OUTDIR"
