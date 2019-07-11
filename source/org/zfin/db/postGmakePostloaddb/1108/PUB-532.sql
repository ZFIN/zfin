--liquibase formatted sql
--changeset pm:PUB-532



drop table if exists tmp_pub;
drop table if exists tmp_pub1;

select pth_pub_zdb_id as pubzdb into tmp_pub from pub_tracking_history
group by pth_pub_zdb_id
having count(pth_pub_zdb_id)=1;





select distinct pubzdb
into tmp_pub1
from tmp_pub,pub_tracking_history, publication,journal
where pubzdb=pth_pub_zdb_id
and pth_pub_zdb_id=zdb_id
and pth_status_id=1
and jtype='Journal'
and pub_jrnl_zdb_id=jrnl_zdb_id
and jrnl_abbrev  like '%Tox%'
and pub_arrival_date between '1996-10-14' and '2005-12-31'
and exists(select 1 from record_attribution
               where pubzdb = recattrib_source_zdb_id)
   and not exists(select 1 from record_attribution
                   where pubzdb = recattrib_source_zdb_id
                     and recattrib_data_zdb_id like 'ZDB-XPAT%')
   and not exists(select 1 from pheno_term
                   where pubzdb = pt_pub_zdb_id);



insert into pub_tracking_history(pth_pub_zdb_id, pth_status_id, pth_status_set_by,pth_location_id)
select pubzdb, 4, 'ZDB-PERS-030520-2',6 from tmp_pub1;
