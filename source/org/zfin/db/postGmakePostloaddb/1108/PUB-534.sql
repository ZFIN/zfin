--liquibase formatted sql
--changeset pm:PUB-534

drop table if exists tmp_pub;
select  pth_pub_zdb_id as pubid
into tmp_pub
from pub_tracking_history, publication,journal
where pth_pub_zdb_id=zdb_id
and pth_status_id=1
and pth_status_is_current='f'
and jtype='Journal'
and pub_jrnl_zdb_id=jrnl_zdb_id
and jrnl_abbrev  like '%Tox%'
and pub_date between '2010-01-01' and '2016-12-31'
and zdb_id not in (select recattrib_source_zdb_id from record_attribution where recattrib_data_zdb_id like 'ZDB-XPAT%')
and pth_pub_zdb_id not in (Select pt_pub_zdb_id from pheno_term)
and zdb_id  in (select pf_pub_Zdb_id from publication_file)
group by pth_pub_zdb_id
having count(pth_pub_zdb_id)=1;

update pub_tracking_history
set pth_status_id=4
from tmp_pub
where pth_pub_zdb_id=pubid;

update pub_tracking_history
set pth_location_id=6
from tmp_pub
where pth_pub_zdb_id=pubid;