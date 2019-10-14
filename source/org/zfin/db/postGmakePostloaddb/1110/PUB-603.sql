--liquibase formatted sql
--changeset pm:PUB-603
drop table if exists tmp_pub;
drop table if exists tmp_pub1;
drop table if exists tmp_pub2;

select pth_pub_zdb_id as pubzdb into tmp_pub from pub_tracking_history
group by pth_pub_zdb_id
having count(pth_pub_zdb_id)=1;

select distinct pubzdb
into tmp_pub1
from tmp_pub, publication,pub_tracking_history
where pubzdb=zdb_id
and pubzdb=pth_pub_zdb_id
and pth_status_id=1
and pth_location_id is null
and jtype='Thesis';

select distinct zdb_id
into tmp_pub2
from  publication
where jtype='Thesis'
and zdb_id not in (select pth_pub_zdb_id from pub_tracking_history);


insert into pub_tracking_history(pth_pub_zdb_id, pth_status_id, pth_status_set_by)
select pubzdb, 12, 'ZDB-PERS-030520-2' from tmp_pub1;

insert into pub_tracking_history(pth_pub_zdb_id, pth_status_id, pth_status_set_by)
select zdb_id, 12, 'ZDB-PERS-030520-2' from tmp_pub2;