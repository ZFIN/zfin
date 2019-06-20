--liquibase formatted sql
--changeset pm:PUB-537
drop table if exists tmp_pub;

select distinct pth_pub_zdb_id as pubid
into tmp_pub
from pub_tracking_history, publication
where pth_pub_zdb_id=zdb_id
and pub_is_indexed='t'
and pth_status_id=4
and pth_location_id is null
and jtype='Journal'
and jrnl_abbrev not  like '%Tox%'
and pub_date between between '0012-07-24' and '2005-12-31'
and zdb_id  in (select recattrib_source_zdb_id from record_attribution)


update pub_tracking_status
set pth_status_id=6
from tmp_pub
where pth_pub_zdb_id=pubid;

update pub_tracking_status
set pth_claimed_by='ZDB-PERS-030612-1'
from tmp_pub
where pth_pub_zdb_id=pubid;