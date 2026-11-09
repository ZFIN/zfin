#!/bin/bash
#
# Prints the pg_dump arguments that leave out the rows of the tables that load_file marks
# lf_exclude_from_dump, one --exclude-table-data=<table> per line. pg_dump keeps the structure,
# indexes and comments of those tables. A table with no load_file row is dumped.
#
# Usage: dump_exclude_args.sh DBNAME      (PGHOST, PGUSER and PGBINDIR are used if set)
#
# If the database cannot be asked (for example load_file does not exist yet), it prints a warning on
# stderr and no arguments, so everything is dumped. Dumping too much is the safe mistake.

db="$1"
if [ -z "$db" ]; then
    echo "usage: $0 DBNAME" >&2
    exit 2
fi

psql_cmd="${PGBINDIR:+$PGBINDIR/}psql"

tables=$("$psql_cmd" -At -d "$db" -c \
    "select lf_table_name from load_file where lf_exclude_from_dump and lf_table_name is not null order by lf_table_name" 2>/dev/null)
if [ $? -ne 0 ]; then
    echo "dump_exclude_args.sh: could not read load_file from $db; dumping every table with its rows" >&2
    exit 0
fi

for table in $tables; do
    # only plain [schema.]table names reach the command line
    if [[ "$table" =~ ^[a-z_][a-z0-9_]*(\.[a-z_][a-z0-9_]*)?$ ]]; then
        echo "--exclude-table-data=$table"
    else
        echo "dump_exclude_args.sh: ignoring unexpected table name: $table" >&2
    fi
done
