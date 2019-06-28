--liquibase formatted sql
--changeset pm:PUB-532

drop table if exists tmp_pub;
select distinct pth_pub_zdb_id as pubid
into tmp_pub
from pub_tracking_history, publication,record_attribution,journal
where pth_pub_zdb_id=zdb_id
and pth_status_id=1
and pth_status_is_current='t'
and jtype='Journal'
and pub_jrnl_zdb_id=jrnl_zdb_id
and jrnl_abbrev  like '%Tox%'
and pub_date between '0012-07-24' and '2005-12-31'
and zdb_id=recattrib_source_zdb_id
and recattrib_data_zdb_id not like 'ZDB-FIG%';

insert into pub_tracking_history(pth_pub_zdb_id, pth_status_id, pth_status_set_by,pth_location_id)
select pubid, 4, 'ZDB-PERS-030520-2',6 from tmp_pub;