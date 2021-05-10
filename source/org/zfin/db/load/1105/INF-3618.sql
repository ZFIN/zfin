--liquibase formatted sql
--changeset sierra:INF-3618.sql

delete from sequence_feature_chromosome_location_generated
 where sfclg_data_zdb_id not in (select zactvd_zdb_id from zdb_active_data);


alter table sequence_feature_chromosome_location_generated
 add constraint sfclg_zdb_active_data_fk foreign key (sfclg_data_zdb_id)
 references zdb_active_data(zactvd_zdb_id)
 on delete cascade;
