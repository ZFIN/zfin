--liquibase formatted sql
--changeset rtaylor:ZFIN-9688-01-pre

delete from tmp_sfclg where sfclg_data_zdb_id not in (select zactvd_zdb_id from zdb_active_data);
insert into sequence_feature_chromosome_location_generated (select * from tmp_sfclg);
DROP TABLE tmp_sfclg;
