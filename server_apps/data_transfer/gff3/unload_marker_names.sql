-- get all of the names, based on vega id

unload to 'marker_names.txt' delimiter '	'

select dblink_acc_num, allmapnm_name, allmapnm_precedence, 0 as gff_feature_id, 
     case
       when allmapnm_precedence = 'Current symbol' then 1 else 0 end as is_display_name
from db_link, all_map_names
where dblink_fdbcont_zdb_id in ('ZDB-FDBCONT-060417-1','ZDB-FDBCONT-040412-14') -- Vega_Trans, VEGA
  and dblink_linked_recid = allmapnm_zdb_id
  and allmapnm_significance <= 6
  and allmapnm_precedence <> 'Orthologue'

union -- add Refseq accessions

select vega.dblink_acc_num, refseq.dblink_acc_num, 'RefSeq',0 as gff_feature_id, 0 as is_display_name  
from db_link vega, db_link refseq
where vega.dblink_fdbcont_zdb_id in ('ZDB-FDBCONT-060417-1','ZDB-FDBCONT-040412-14') -- Vega_Trans, VEGA
  and refseq.dblink_fdbcont_zdb_id = 'ZDB-FDBCONT-040412-38' -- RefSeq
  and vega.dblink_linked_recid = refseq.dblink_linked_recid

union -- add zdb_id

select dblink_acc_num, dblink_linked_recid, 'zdb_id',0 as gff_feature_id, 0 as is_display_name  
from db_link
where dblink_fdbcont_zdb_id in ('ZDB-FDBCONT-060417-1','ZDB-FDBCONT-040412-14') -- Vega_Trans, VEGA
;
