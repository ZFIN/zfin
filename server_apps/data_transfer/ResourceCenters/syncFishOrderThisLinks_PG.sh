#!/bin/bash

echo 'delete from int_data_supplier where idsup_data_zdb_id like 'ZDB-FISH-%' | ${PGBINDIR}/psql <!--|DB_NAME|-->

echo 'insert into int_data_supplier (idsup_data_zdb_id, idsup_supplier_zdb_id) select fish_Zdb_id, a.idsup_supplier_zdb_id from int_data_supplier a, fish where a.idsup_data_zdb_id = fish_genotype_zdb_id and not exists (Select 'x' from fish_str  where fishstr_fish_zdb_id = fish_zdb_id) and not exists (select "x" from int_data_Supplier b where b.idsup_data_zdb_id = fish_zdb_id and b.idsup_supplier_zdb_id = a.idsup_supplier_zdb_id)' | $INFORMIXDIR/bin/psql $DBNAME

