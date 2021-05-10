--liquibase formatted sql
--changeset pm:ZFIN-5893




create table  ensdarg_post_proc (
ensdargidpost text not null,
        geneid text not null,
           dblinkid text not null);

insert into ensdarg_post_proc (ensdargidpost, geneid, dblinkid) select distinct  ensdarg, dblink_linked_recid, dblink_zdb_id from inputensdarg,db_link where ensdarg=dblink_acc_num;
insert into record_attribution (recattrib_data_zdb_id,recattrib_source_zdb_id) select distinct geneid,'ZDB-PUB-170214-96' from ensdarg_post_proc where geneid not in (select recattrib_data_zdb_id from record_attribution where recattrib_source_zdb_id='ZDB-PUB-170214-96');

insert into record_attribution (recattrib_data_zdb_id,recattrib_source_zdb_id) select distinct dblinkid,'ZDB-PUB-170214-96' from ensdarg_post_proc where dblinkid not in (select recattrib_data_zdb_id from record_attribution where recattrib_source_zdb_id='ZDB-PUB-170214-96');


