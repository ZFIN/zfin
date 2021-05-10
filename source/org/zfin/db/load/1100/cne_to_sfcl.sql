--liquibase formatted sql
--changeset sierra:cne_to_sfcl.sql

alter table sequence_feature_chromosome_location
 drop constraint sfcl_feature_zdb_id_fk_odc ;

alter table sequence_feature_chromosome_location 
 add constraint sfcl_feature_zdb_id_fk_odc foreign key (sfcl_feature_zdb_id)
 references zdb_active_data on delete cascade;
