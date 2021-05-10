--liquibase formatted sql
--changeset pm:PUB-530



drop table if exists tmp_pub;
drop table if exists tmp_pub1;
drop table if exists tmp_figure;
drop table if exists tmp_pub2;
drop table if exists tmp_pub3;


select distinct fig_source_zdb_id as figsource
into tmp_figure
from figure;

select figsource
into tmp_pub1
from tmp_figure
where exists (select 1 from record_attribution
               where figsource = recattrib_source_zdb_id)
   and not exists(select 1 from record_attribution
                   where figsource = recattrib_source_zdb_id
                     and recattrib_data_zdb_id not like  'ZDB-FIG%'
and recattrib_data_zdb_id not like 'ZDB-IMAGE%');

select figsource
into tmp_pub2
from tmp_pub1, publication
where figsource=zdb_id
and pub_arrival_date between  '1996-10-14' and '2005-12-31'
and jtype='Journal';

select pth_pub_zdb_id as pubzdb into tmp_pub from pub_tracking_history
where exists(select 1 from publication_file
where pth_pub_zdb_id = pf_pub_zdb_id)
group by pth_pub_zdb_id
having count(pth_pub_zdb_id)=1;







select distinct pubzdb
into tmp_pub3
from tmp_pub,tmp_pub2
where pubzdb=figsource;






insert into pub_tracking_history(pth_pub_zdb_id, pth_status_id, pth_status_set_by)
select pubzdb, 13, 'ZDB-PERS-030520-2' from tmp_pub3;

