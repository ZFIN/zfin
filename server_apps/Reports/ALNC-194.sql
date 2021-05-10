select mrkr_zdb_id  from db_link, marker_relationship, clone
,marker
where dblink_linked_recid = mrel_mrkr_2_zdb_id
and mrel_mrkr_2_zdb_id=clone_mrkr_zdb_id
and mrel_mrkr_1_zdb_id not in (select dblink_linked_recid from db_link)
and mrel_mrkr_2_zdb_id = clone_mrkr_zdb_id
and clone_problem_type like '%Chimeric%' and mrel_mrkr_1_zdb_id=mrkr_zdb_id;
