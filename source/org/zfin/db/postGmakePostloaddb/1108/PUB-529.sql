--liquibase formatted sql
--changeset pm:PUB-529


drop table if exists tmp_pub;
drop table if exists tmp_pub1;

select pth_pub_zdb_id as pubzdb into tmp_pub from pub_tracking_history
group by pth_pub_zdb_id
having count(pth_pub_zdb_id)=1;


select distinct pubzdb
into tmp_pub1
from tmp_pub, pub_tracking_history, publication,record_attribution,journal
where pubzdb=pth_pub_zdb_id and pth_pub_zdb_id=zdb_id
and pth_status_id=1
and jtype='Journal'
and pub_jrnl_zdb_id=jrnl_zdb_id
and jrnl_abbrev not like '%Tox%'
and pub_arrival_date between '1996-10-14' and '2005-12-31'
and zdb_id=recattrib_source_zdb_id order by pubzdb;



insert into pub_tracking_history(pth_pub_zdb_id, pth_status_id, pth_status_set_by)
select pubzdb, 11, 'ZDB-PERS-030520-2' from tmp_pub1;



