--liquibase formatted sql
--changeset pm:PUB-524

drop table if exists tmp_pub;

select  pth_pub_zdb_id as pubid
into tmp_pub
from pub_tracking_history, publication
where pth_pub_zdb_id=zdb_id
and jtype='Journal'
and pub_date between '2010-01-01' and '2016-12-31'
and pth_status_id=1
and zdb_id not in (select recattrib_source_zdb_id from record_attribution)
and zdb_id  not in (select pf_pub_Zdb_id from publication_file)
and pth_status_is_current='t';

insert into pub_tracking_history(pth_pub_zdb_id, pth_status_id, pth_status_set_by)
select pubid, 15, 'ZDB-PERS-030520-2' from tmp_pub;
