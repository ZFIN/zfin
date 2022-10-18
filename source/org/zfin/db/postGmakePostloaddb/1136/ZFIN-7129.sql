--liquibase formatted sql
--changeset cmpich:ZFIN-7129

-- copy data into new table with count of DB_LINKs
insert into ensdarg_tt_no_dups
select ensdarg, zfinid, count(*) as ct
from ensdarg_tt,
     db_link
where ensdarg = dblink_acc_num
  and dblink_fdbcont_zdb_id = 'ZDB-FDBCONT-061018-1'
group by ensdarg, zfinid;

-- update table to point to DB_LINK records for singleton
update ensdarg_tt_no_dups
set d_dblinkid = (select dblink_zdb_id
                  from                        db_link
                  where d_ensdarg = dblink_acc_num
                    and dblink_fdbcont_zdb_id = 'ZDB-FDBCONT-061018-1')
where d_count = 1;

-- attribute the singleton records
insert into record_attribution (recattrib_data_zdb_id, recattrib_source_zdb_id)
select d_dblinkid, 'ZDB-PUB-170202-7'
from ensdarg_tt_no_dups where d_count = 1;
