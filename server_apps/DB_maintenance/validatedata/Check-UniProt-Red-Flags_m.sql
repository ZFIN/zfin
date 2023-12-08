select dblink_acc_num, string_agg(dblink_linked_recid, ','), count(dblink_linked_recid)
from db_link
where dblink_fdbcont_zdb_id in (
                                'ZDB-FDBCONT-040412-38',
                                'ZDB-FDBCONT-040412-39',
                                'ZDB-FDBCONT-040527-1',
                                'ZDB-FDBCONT-041217-2'
    )
  and dblink_linked_recid like 'ZDB-GENE-%'
group by dblink_acc_num
having count(dblink_linked_recid) > 1;
