--liquibase formatted sql
--changeset pm:PUB-524


drop table if exists tmp_pub;
drop table if exists tmp_pub1;

select pth_pub_zdb_id as pubzdb into tmp_pub from pub_tracking_history
where not exists(select 1 from publication_file
where pth_pub_zdb_id = pf_pub_zdb_id)
group by pth_pub_zdb_id
having count(pth_pub_zdb_id)=1;

select  pubzdb
into tmp_pub1
from tmp_pub,pub_tracking_history, publication
where pubzdb=pth_pub_zdb_id and pth_pub_zdb_id=zdb_id
and jtype='Journal'
and pub_date between '2010-01-01' and '2016-12-31'
and pth_status_id=1
and zdb_id not in (select recattrib_source_zdb_id from record_attribution);
--and pth_status_is_current='f';

--\copy (select * from tmp_pub1 order by pubzdb) to 'pub524.csv' delimiter ',';
insert into pub_tracking_history(pth_pub_zdb_id, pth_status_id, pth_status_set_by)
select pubzdb, 15, 'ZDB-PERS-030520-2' from tmp_pub1;
