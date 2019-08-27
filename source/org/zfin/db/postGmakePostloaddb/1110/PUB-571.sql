--liquibase formatted sql
--changeset pm:PUB-571


drop table if exists tmp_pub;
drop table if exists tmp_pub1;

select pth_pub_zdb_id as pub_id
into tmp_pub
from pub_tracking_history,publication
where
pth_status_id=13
and pth_pub_zdb_id=zdb_id
and  pub_arrival_date between '1996-10-14' and '2005-12-31'
and pth_status_is_current='t'
and pth_pub_zdb_id in (select recattrib_source_zdb_id from record_attribution);

select distinct pub_id,cur_curator_zdb_id
into tmp_pub1
from tmp_pub,curation
where pub_id = cur_pub_zdb_id and cur_closed_date is not null
group by pub_id,cur_curator_zdb_id having count(cur_curator_zdb_id)>4;


insert into pub_tracking_history(pth_pub_zdb_id, pth_status_id, pth_status_set_by,pth_status_is_current )
select distinct pub_id, 11, cur_curator_zdb_id,'t' from tmp_pub1;


