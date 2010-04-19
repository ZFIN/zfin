unload to 'clone_lengths.txt' delimiter '	'
select mrkr_abbrev, dblink_length
from  db_link, marker, marker_type_group_member
where dblink_fdbcont_zdb_id = 'ZDB-FDBCONT-040412-36' -- genbank
  and mrkr_zdb_id = dblink_linked_recid
  and mrkr_type = mtgrpmem_mrkr_type
  and mtgrpmem_mrkr_type_group = 'CLONE';



