--liquibase formatted sql
--changeset pm:PUB-537
drop table if exists tmp_pub;

select distinct pth_pub_zdb_id as pubid
into tmp_pub
from pub_tracking_history, publication,journal
where pth_pub_zdb_id=zdb_id
and pub_jrnl_zdb_id=jrnl_zdb_id
and pub_is_indexed='t'
and pth_status_id=4
and pth_status_is_current='t'
and pth_location_id is null
and jtype='Journal'
and jrnl_abbrev not  like '%Tox%'
and pub_date  between '0012-07-24' and '2005-12-31'
and pth_pub_zdb_id not in (select recattrib_source_zdb_id from record_attribution);



insert into pub_tracking_history(pth_pub_zdb_id, pth_status_id, pth_status_set_by)
select pubzdb, 12, 'ZDB-PERS-030520-2' from tmp_pub;