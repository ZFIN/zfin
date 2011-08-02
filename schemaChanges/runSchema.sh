#!/bin/sh
dbaccess -a $DBNAME /research/zunloads/projects/genePage/schema.sql

dbaccess -a $DBNAME schema.sql

update foreign_db 
set fdb_db_query='http://www.ensembl.org/Danio_rerio/Variation/Summary?db=core;vdb=variation;v='
where fdb_db_name = 'Ensembl_SNP'

