--liquibase formatted sql
--changeset pm:PUB-554

drop table if exists tmp_pub;



select distinct pth_pub_zdb_id as pubid
into tmp_pub
from pub_tracking_history, publication
where pth_pub_zdb_id=zdb_id
and pth_status_id=4
and pth_location_id is null
and jtype='Journal'
and pub_arrival_date between '1996-10-14' and '2005-12-31'
and zdb_id  in (select recattrib_source_zdb_id from record_attribution);
--and pth_status_is_current='t';


insert into pub_tracking_history(pth_pub_zdb_id, pth_status_id, pth_status_set_by)
select pubid, 11, 'ZDB-PERS-030520-2' from tmp_pub;
