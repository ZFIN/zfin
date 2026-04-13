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

echo "Cleaning all_term_contains_old_ tables";
echo "select regen_cleanup_renamed_tables('all_term_contains_old_')" | ${PGBINDIR}/psql -v ON_ERROR_STOP=1 $DBNAME;
if [ $? -ne 0 ]; then
    echo "regen_cleanup_renamed_tables('all_term_contains_old_') failed";
    exit 1;
fi

echo "Cleaning pheno_term_fast_search_old_ tables";
echo "select regen_cleanup_renamed_tables('pheno_term_fast_search_old_')" | ${PGBINDIR}/psql -v ON_ERROR_STOP=1 $DBNAME;
if [ $? -ne 0 ]; then
    echo "regen_cleanup_renamed_tables('pheno_term_fast_search_old_') failed";
    exit 1;
fi

for i in phenotype_source_generated phenotype_observation_generated phenotype_generated_curated_mapping
do
    TABLE=${i}_old_
    echo "Cleaning $TABLE";
    echo "select regen_cleanup_renamed_tables('$TABLE')" | ${PGBINDIR}/psql -v ON_ERROR_STOP=1 $DBNAME;
    if [ $? -ne 0 ]; then
        echo "regen_cleanup_renamed_tables('$TABLE') failed";
        exit 1;
    fi
done

date;
echo "done with regen finish cleanup";
