--liquibase formatted sql
--changeset pm:PUB-525

drop table if exists tmp_pub;
select pth_pub_zdb_id as pubid
into tmp_pub
from pub_tracking_history, publication
where pth_pub_zdb_id=zdb_id
and pth_status_id=1
and pth_status_is_current='f'
and jtype='Journal'
and pub_date between '2010-01-01' and '2016-12-31'
and zdb_id not in (select recattrib_source_zdb_id from record_attribution where recattrib_data_zdb_id not like 'ZDB-FIG%')
and zdb_id  in (select pf_pub_Zdb_id from publication_file)
group by pth_pub_zdb_id
having count(pth_pub_zdb_id)=1;

update pub_tracking_history
set pth_status_id=2
from tmp_pub
where pth_pub_zdb_id=pubid;