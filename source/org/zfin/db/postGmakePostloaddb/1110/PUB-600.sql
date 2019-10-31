--liquibase formatted sql
--changeset pm:PUB-600


drop table if exists tmp_pub;
drop table if exists tmp_pub1;


select pth_pub_zdb_id as pubzdb into tmp_pub from pub_tracking_history
where pth_status_id=4 and pth_location_id is null;

select distinct pubzdb
into tmp_pub1
from tmp_pub,pub_tracking_history, publication,journal,record_attribution
where pubzdb=pth_pub_zdb_id
and pth_pub_zdb_id=zdb_id
and pth_status_is_current='t'
and pth_status_id=4
and jtype='Journal'
and pub_jrnl_zdb_id=jrnl_zdb_id
and pub_arrival_date between '2010-01-01' and '2017-12-31'
and zdb_id not in (select recattrib_source_zdb_id from record_attribution where (recattrib_data_zdb_id not like 'ZDB-XPAT%'
and recattrib_data_zdb_id not like 'ZDB-ORTHO%'
and recattrib_Data_zdb_id not like 'ZDB-DAT%' and recattrib_data_zdb_id not like '%MRKRGOEV%'))
 and pth_pub_zdb_id not in (Select pt_pub_zdb_id from pheno_term);


insert into pub_tracking_history(pth_pub_zdb_id, pth_status_id, pth_status_set_by)
select distinct pubzdb, 12, 'ZDB-PERS-030520-2' from tmp_pub1;


