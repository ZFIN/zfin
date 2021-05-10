--liquibase formatted sql
--changeset pm:PUB-529


drop table if exists tmp_pub;
drop table if exists tmp_pub1;


drop table if exists tmp_pub4;
drop table if exists tmp_pub5;
drop table if exists tmp_figure;
drop table if exists tmp_pub2;
drop table if exists tmp_pub3;


select distinct fig_source_zdb_id as figsource
into tmp_figure
from figure;

select figsource
into tmp_pub5
from tmp_figure
where exists (select 1 from record_attribution
               where figsource = recattrib_source_zdb_id)
   and not exists(select 1 from record_attribution
                   where figsource = recattrib_source_zdb_id
                     and recattrib_data_zdb_id not like  'ZDB-FIG%'
and recattrib_data_zdb_id not like 'ZDB-IMAGE%');






select pth_pub_zdb_id as pubzdb into tmp_pub from pub_tracking_history
group by pth_pub_zdb_id
having count(pth_pub_zdb_id)=1;


select distinct pubzdb
into tmp_pub1
from tmp_pub, pub_tracking_history, publication,record_attribution,journal
where pubzdb=pth_pub_zdb_id and pth_pub_zdb_id=zdb_id
and pth_status_id=1
and jtype='Journal'
and pub_jrnl_zdb_id=jrnl_zdb_id
and jrnl_abbrev not like '%Tox%'
and pub_arrival_date between '1996-10-14' and '2005-12-31'
and zdb_id=recattrib_source_zdb_id and zdb_id not in (select figsource from tmp_pub5) order by pubzdb;



insert into pub_tracking_history(pth_pub_zdb_id, pth_status_id, pth_status_set_by)
select pubzdb, 11, 'ZDB-PERS-030520-2' from tmp_pub1;
