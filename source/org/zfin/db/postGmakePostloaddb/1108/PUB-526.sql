--liquibase formatted sql
--changeset pm:PUB-526




drop table if exists tmp_pub;
drop table if exists tmp_pub1;

select pth_pub_zdb_id as pubzdb into tmp_pub from pub_tracking_history
where exists(select 1 from publication_file
where pth_pub_zdb_id = pf_pub_zdb_id)
group by pth_pub_zdb_id
having count(pth_pub_zdb_id)=1;

select distinct pubzdb
into tmp_pub1
from tmp_pub,pub_tracking_history, publication,journal,record_attribution
where pubzdb=pth_pub_zdb_id
and pth_pub_zdb_id=zdb_id
and pth_status_id=1
--and pth_status_is_current='f'
and jtype='Journal'
and pub_jrnl_zdb_id=jrnl_zdb_id
and jrnl_abbrev  not like '%Tox%'
and pub_arrival_date between '2010-01-01' and '2016-12-31'
and zdb_id = recattrib_source_zdb_id and recattrib_data_zdb_id not like 'ZDB-XPAT%'
and pth_pub_zdb_id not in (Select pt_pub_zdb_id from pheno_term);



insert into pub_tracking_history(pth_pub_zdb_id, pth_status_id, pth_status_set_by,pth_claimed_by)
select distinct pubzdb, 6, 'ZDB-PERS-030520-2','ZDB-PERS-100329-1' from tmp_pub1;


