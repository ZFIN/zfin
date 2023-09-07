#!/bin/bash

echo "Dropping tables that were renamed earlier";
for i in clean_expression mutant genotype_figure
do
    TABLE=${i}_fast_search_old_
    echo "Cleaning $TABLE";
    echo "select regen_cleanup_renamed_tables('$TABLE')" | ${PGBINDIR}/psql -v ON_ERROR_STOP=1 $DBNAME;
    if [ $? -ne 0 ]; then
        echo "regen_cleanup_renamed_tables('$TABLE') failed";
        exit 1;
    fi
done

date;
echo "done with regen finish cleanup";
