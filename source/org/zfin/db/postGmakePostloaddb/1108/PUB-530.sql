--liquibase formatted sql
--changeset pm:PUB-530

drop table if exists tmp_pub;
select distinct pth_pub_zdb_id as pubid
into tmp_pub
from pub_tracking_history, publication,record_attribution
where pth_pub_zdb_id=zdb_id
and pth_status_id=1
and jtype='Journal'
and pub_date between '0012-07-24' and '2005-12-31'
and zdb_id=recattrib_source_zdb_id
and recattrib_data_zdb_id  like 'ZDB-FIG%';
update pub_tracking_history
set pth_status_id=12
from tmp_pub
where pth_pub_zdb_id=pubid;