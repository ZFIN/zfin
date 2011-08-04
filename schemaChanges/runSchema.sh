#!/bin/sh
dbaccess -a $DBNAME /research/zunloads/projects/genePage/schema.sql

dbaccess -a $DBNAME schema.sql

#unload to 'geo_record_attributions.txt'
#(
#select * from record_attribution ra
#where ra.recattrib_source_zdb_id =  'ZDB-PUB-071218-1'
#and ra.recattrib_data_zdb_id not like 'ZDB-DBLINK%'
#)

echo "delete from record_attribution where recattrib_source_zdb_id =  'ZDB-PUB-071218-1' and recattrib_data_zdb_id like 'ZDB-DBLINK%'  ;" | dbaccess -a $DBNAME ;

// loads geo microarray records
load from 'geo_record_attributions.txt' insert into record_attribution ; 



