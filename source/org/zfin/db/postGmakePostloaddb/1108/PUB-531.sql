--liquibase formatted sql
--changeset pm:PUB-531

drop table if exists tmp_pub;
select distinct pth_pub_zdb_id as pubid
into tmp_pub
from pub_tracking_history, publication
where pth_pub_zdb_id=zdb_id
and pth_status_id=1
and jtype='Journal'
and pub_date between '0012-07-24' and '2009-12-31'
and zdb_id not in (select recattrib_source_zdb_id from record_attribution)

update pub_tracking_history
set pth_status_id=13
from tmp_pub
where pth_pub_zdb_id=pubid;