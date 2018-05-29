--liquibase formatted sql
--changeset pm:ZFIN-5951


delete from record_attribution
where recattrib_data_zdb_id in ('ZDB-CRISPR-180514-2','ZDB-MRPHLNO-180514-1')
and recattrib_source_zdb_id='ZDB-PUB-171216-9';





