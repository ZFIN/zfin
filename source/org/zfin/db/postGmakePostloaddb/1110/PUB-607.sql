--liquibase formatted sql
--changeset pm:PUB-607

select pth_pub_zdb_id as pubzdb into tmp_pub from pub_tracking_history
group by pth_pub_zdb_id
having count(pth_pub_zdb_id)=1;

select distinct pubzdb
into tmp_pub1
from tmp_pub, publication,pub_tracking_history
where pubzdb=zdb_id
and year(pub_date) in ('2017','2018','2019')
and pubzdb=pth_pub_zdb_id
and pth_status_id=1
and pth_location_id is null
and jtype='Journal'
and status='active';



insert into pub_tracking_history(pth_pub_zdb_id, pth_status_id, pth_status_set_by)
select pubzdb, 20, 'ZDB-PERS-030520-2' from tmp_pub1;