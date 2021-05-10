
select a.mrkr_abbrev
 from marker a, marker_relationship, marker b
 where b.mrkr_name like 'MGC%'
and a.mrkr_Zdb_id = mrel_mrkr_1_zdb_id
 and b.mrkr_zdb_id = mrel_mrkr_2_zdb_id
and not exists (Select 'x' from db_link, foreign_db_contains
                       where dblink_Fdbcont_zdb_id =fdbcont_zdb_id
                       and fdbcont_Zdb_id  = 'ZDB-FDBCONT-040412-1'
                       and dblink_linked_Recid = a.mrkr_Zdb_id)
and to_date(get_date_from_id(a.mrkr_zdb_id,'YYYY-MM-DD'),'YYYY-MM-DD') > current_date - interval '30' day;