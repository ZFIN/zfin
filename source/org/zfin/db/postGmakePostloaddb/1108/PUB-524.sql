--liquibase formatted sql
--changeset pm:PUB-524

select distinct pth_pub_zdb_id as pubid
into tmp_pub
from pub_tracking_history, publication
where pth_pub_zdb_id=zdb_id
and pth_status_id=1
and jtype='Journal'
and pub_date between '2010-01-01' and '2016-12-31'
and zdb_id not in (select recattrib_source_zdb_id from record_attribution)
and zdb_id not in (select pf_pub_Zdb_id from publication_file);

update pub_tracking_status
set pth_status_id=15
from tmp_pub
where pth_pub_zdb_id=pubid;