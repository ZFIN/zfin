--liquibase formatted sql
--changeset prita:CUR-427

insert into  record_attribution (recattrib_data_Zdb_id,recattrib_source_zdb_id,recattrib_source_type)
select recattrib_data_zdb_id,recattrib_source_zdb_id,'standard'
from record_attribution
where recattrib_data_Zdb_id like 'ZDB-ALT%' and recattrib_source_type!='standard'
and recattrib_data_zdb_id not in (select recattrib_data_Zdb_id from record_attribution where recattrib_data_zdb_id like 'ZDB-ALT%' and recattrib_source_type='standard');