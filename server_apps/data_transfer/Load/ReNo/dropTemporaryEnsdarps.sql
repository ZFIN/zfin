begin work ;

create temp table tmp_ids (dblink_zdb_id text);

insert into tmp_ids
select dblink_zdb_id
  from db_link
 where dblink_acc_num like 'ENSDARP%';

delete from zdb_active_data
 where zactvd_zdb_id in (select dblink_Zdb_id from tmp_ids);

commit work ;
