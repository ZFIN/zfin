--liquibase formatted sql
--changeset pm:CUR-732

update record_attribution set recattrib_source_zdb_id='ZDB-PUB-180105-5' where recattrib_source_zdb_id='ZDB-PUB-030508-1' and recattrib_data_zdb_id like 'ZDB-ALT%';
update record_attribution set recattrib_source_zdb_id='ZDB-PUB-180105-5' where recattrib_source_zdb_id='ZDB-PUB-030508-1' and recattrib_data_zdb_id like 'ZDB-GENO%';
