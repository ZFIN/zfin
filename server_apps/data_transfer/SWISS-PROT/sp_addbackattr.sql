-- sp_adbackattr.sql
-- Somehow, error happened during the UniProt load on 01/07/2010, with many records not attributed to 
-- anything. This cript is to add back the attribution.

begin work;

!echo 'Create temp table uniprot_records_without_attri'
create temp table uniprot_records_without_attri (
               uniprot_records_zdb_id varchar(50)
            ) with no log;

insert into uniprot_records_without_attri (uniprot_records_zdb_id)     
select dblink_zdb_id 
from db_link 
where dblink_info like "%Swiss-Prot%" 
and not exists (select * from record_attribution where recattrib_data_zdb_id = dblink_zdb_id);
!echo '		into temp table uniprot_records_without_attri'

!echo 'Attribute db links to the internal pub record ZDB-PUB-020723-2'
	insert into record_attribution (recattrib_data_zdb_id, recattrib_source_zdb_id)
		select uniprot_records_zdb_id, "ZDB-PUB-020723-2"
		  from uniprot_records_without_attri;
!echo '		into record_attribution'

delete from uniprot_records_without_attri;
!echo '		from temp table uniprot_records_without_attri'

insert into uniprot_records_without_attri (uniprot_records_zdb_id)     
select mrkrgoev_zdb_id 
from marker_go_term_evidence 
where mrkrgoev_evidence_code = "IEA" 
and mrkrgoev_annotation_organization = "5"
and mrkrgoev_source_zdb_id = "ZDB-PUB-020723-1" 
and not exists (select * from record_attribution where recattrib_data_zdb_id = mrkrgoev_zdb_id);
!echo '		into temp table uniprot_records_without_attri'

!echo 'Attribute SP keyword 2 GO records to the internal pub record ZDB-PUB-020723-1'
	insert into record_attribution (recattrib_data_zdb_id, recattrib_source_zdb_id)
		select uniprot_records_zdb_id, "ZDB-PUB-020723-1"
		  from uniprot_records_without_attri;
!echo '		into record_attribution'

delete from uniprot_records_without_attri;
!echo '		from temp table uniprot_records_without_attri'

insert into uniprot_records_without_attri (uniprot_records_zdb_id)     
select mrkrgoev_zdb_id 
from marker_go_term_evidence 
where mrkrgoev_evidence_code = "IEA" 
and mrkrgoev_annotation_organization = "5"
and mrkrgoev_source_zdb_id = "ZDB-PUB-020724-1" 
and not exists (select * from record_attribution where recattrib_data_zdb_id = mrkrgoev_zdb_id);
!echo '		into temp table uniprot_records_without_attri'

!echo 'Attribute InterPro 2 GO records to the internal pub record ZDB-PUB-020724-1'
	insert into record_attribution (recattrib_data_zdb_id, recattrib_source_zdb_id)
		select uniprot_records_zdb_id, "ZDB-PUB-020724-1"
		  from uniprot_records_without_attri;
!echo '		into record_attribution'

delete from uniprot_records_without_attri;
!echo '		from temp table uniprot_records_without_attri'

insert into uniprot_records_without_attri (uniprot_records_zdb_id)     
select mrkrgoev_zdb_id 
from marker_go_term_evidence 
where mrkrgoev_evidence_code = "IEA" 
and mrkrgoev_annotation_organization = "5"
and mrkrgoev_source_zdb_id = "ZDB-PUB-031118-3" 
and not exists (select * from record_attribution where recattrib_data_zdb_id = mrkrgoev_zdb_id);
!echo '		into temp table uniprot_records_without_attri'

!echo 'Attribute EC acc 2 GOrecords to the internal pub record ZDB-PUB-031118-3'
	insert into record_attribution (recattrib_data_zdb_id, recattrib_source_zdb_id)
		select uniprot_records_zdb_id, "ZDB-PUB-031118-3"
		  from uniprot_records_without_attri;
!echo '		into record_attribution'

delete from uniprot_records_without_attri;
!echo '		from temp table uniprot_records_without_attri'

insert into uniprot_records_without_attri (uniprot_records_zdb_id)     
select distinct extnote_zdb_id 
from external_note, db_link 
where extnote_data_zdb_id = dblink_zdb_id 
and dblink_info like "%Swiss-Prot%"
and not exists (select * from record_attribution where recattrib_data_zdb_id = extnote_zdb_id);
!echo '		into temp table uniprot_records_without_attri'

!echo 'Attribute external_note to the internal pub record ZDB-PUB-020723-2'
	insert into record_attribution (recattrib_data_zdb_id, recattrib_source_zdb_id)
		select uniprot_records_zdb_id, "ZDB-PUB-020723-2"
		  from uniprot_records_without_attri;
!echo '		into record_attribution'

--rollback work;
commit work;
