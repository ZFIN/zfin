--liquibase formatted sql
--changeset pm:PUB-534

select distinct pth_pub_zdb_id as pubid
into tmp_pub
from pub_tracking_history, publication,record_attribution,journal
where pth_pub_zdb_id=zdb_id
and pth_status_id=1
and jtype='Journal'
and pub_jrnl_zdb_id=jrnl_zdb_id
and jrnl_abbrev  like '%Tox%'
and pub_date between '2010-01-01' and '2016-12-31'
and zdb_id=recattrib_source_zdb_id
and recattrib_data_zdb_id not like 'ZDB-XPAT%'
and pth_pub_zdb_id not in (Select pt_pub_zdb_id from pheno_term)
and zdb_id  in (select pf_pub_Zdb_id from publication_file);

update pub_tracking_history
set pth_status_id=4
from tmp_pub
where pth_pub_zdb_id=pubid;

update pub_tracking_history
set pth_location_id=6
from tmp_pub
where pth_pub_zdb_id=pubid;