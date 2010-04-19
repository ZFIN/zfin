unload to 'zdb_ids.txt' delimiter '	'
select mrkr_abbrev, mrkr_zdb_id
from marker, marker_type_group_member
where mrkr_type = mtgrpmem_mrkr_type
and mtgrpmem_mrkr_type_group in ('GENEDOM','CLONE','TRANSCRIPT');


