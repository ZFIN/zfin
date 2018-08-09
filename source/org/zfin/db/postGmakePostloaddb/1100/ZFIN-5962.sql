--liquibase formatted sql
--changeset pm:ZFIN-5962


INSERT INTO record_attribution (recattrib_data_zdb_id,recattrib_source_zdb_id,recattrib_source_type)
VALUES ('ZDB-CRISPR-160328-1', 'ZDB-PUB-141217-17', 'standard');
INSERT INTO record_attribution (recattrib_data_zdb_id,recattrib_source_zdb_id,recattrib_source_type)
VALUES ('ZDB-CRISPR-170830-1', 'ZDB-PUB-170103-4', 'standard');
INSERT INTO record_attribution (recattrib_data_zdb_id,recattrib_source_zdb_id,recattrib_source_type)
VALUES ('ZDB-CRISPR-180419-3', 'ZDB-PUB-180105-1', 'standard');
