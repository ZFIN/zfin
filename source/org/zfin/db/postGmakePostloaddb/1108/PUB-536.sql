--liquibase formatted sql
--changeset pm:PUB-536
drop table if exists tmp_pub;
select distinct pth_pub_zdb_id as pubid
into tmp_pub
from pub_tracking_history, publication,record_attribution,journal
where pth_pub_zdb_id=zdb_id
and pth_status_id=4
and pth_status_is_current='t'
and pth_location_id is null
and pub_is_indexed='f'
and jtype='Journal'
and pub_jrnl_zdb_id=jrnl_zdb_id
and jrnl_abbrev not like '%Tox%'
and pub_arrival_date between '0012-07-24' and '2005-12-31'
and zdb_id=recattrib_source_zdb_id


update pub_tracking_history
set pth_status_id=11
from tmp_pub
where pth_pub_zdb_id=pubid;