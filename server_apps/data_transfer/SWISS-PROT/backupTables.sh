#!/bin/bash -e

# This script can be useful for troubleshooting a uniprot load
# It captures the state of all the tables that are likely to change during the course of the uniprot run
# This way you can easily compare a before and after state of each table
# Also, if you need to revert back to the DB state from before a uniprot run, you can do that more quickly
# than with a gradle loaddb full restore.
#

if [ -z "$DBNAME" ]; then
	echo "No DBNAME"
	exit 1
fi

TIMESTAMP=`date +%Y-%m-%d-%H-%M-%S`
DIRECTORY=archives/db/$TIMESTAMP
mkdir -p $DIRECTORY/csv
mkdir -p $DIRECTORY/sql
TABLES_WITH_1_COLUMN="zdb_active_data"
TABLES_WITH_2_COLUMN="noctua_model_annotation marker_go_term_annotation_extension_group"
TABLES="db_link external_note inference_group_member interpro_protein marker_go_term_annotation_extension marker_go_term_evidence marker_to_protein protein protein_to_interpro protein_to_pdb record_attribution tables_in_trouble"

echo "Making csv backups "  $(date "+%Y-%m-%d %H:%M:%S")
for t in $TABLES
do
  echo " $t to $DIRECTORY/csv/$t.csv"
  echo "copy (select * from $t order by 1,2,3) to stdout with csv header" | psql -v ON_ERROR_STOP=1 $DBNAME > $DIRECTORY/csv/$t.csv
done

for t in $TABLES_WITH_1_COLUMN
do
  echo " $t to $DIRECTORY/csv/$t.csv"
  echo "copy (select * from $t order by 1) to stdout with csv header" | psql -v ON_ERROR_STOP=1 $DBNAME > $DIRECTORY/csv/$t.csv
done

for t in $TABLES_WITH_2_COLUMN
do
  echo " $t to $DIRECTORY/csv/$t.csv"
  echo "copy (select * from $t order by 1,2) to stdout with csv header" | psql -v ON_ERROR_STOP=1 $DBNAME > $DIRECTORY/csv/$t.csv
done

echo "Making sql backups "  $(date "+%Y-%m-%d %H:%M:%S")
for t in $TABLES $TABLES_WITH_1_COLUMN $TABLES_WITH_2_COLUMN
do
  echo " $t to $DIRECTORY/sql/$t.sql"
  pg_dump -d $DBNAME --table $t > $DIRECTORY/sql/$t.sql
done

echo "Finished "  $(date "+%Y-%m-%d %H:%M:%S")

touch "$DIRECTORY/README.txt"

#line counts
cd $DIRECTORY/csv
wc -l *.csv > wc.txt
cd -

echo $* > "$DIRECTORY/README.txt"

echo "Consider adding context information to $DIRECTORY/README.txt about this snapshot."