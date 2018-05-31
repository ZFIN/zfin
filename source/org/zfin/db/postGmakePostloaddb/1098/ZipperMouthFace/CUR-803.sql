--liquibase formatted sql
--changeset Christian:CUR-803


delete from record_attribution where  recattrib_data_zdb_id = 'ZDB-FISH-150901-28051' and recattrib_source_zdb_id  = 'ZDB-PUB-150729-10';

delete from record_attribution where  recattrib_data_zdb_id = 'ZDB-GENO-071024-1' and recattrib_source_zdb_id  = 'ZDB-PUB-150729-10';