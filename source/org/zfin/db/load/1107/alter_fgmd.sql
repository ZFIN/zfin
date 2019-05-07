--liquibase formatted sql
--changeset pm:alter_fgmd.sql

alter table feature_genomic_mutation_detail
  add constraint fgmd_zdb_id_fk_odc
  foreign key (fgmd_zdb_id)
  references zdb_active_data(zactvd_zdb_id) on delete cascade;

ALTER TABLE feature_genomic_mutation_detail ALTER COLUMN fgmd_sequence_of_variation  DROP NOT NULL;

ALTER TABLE feature_genomic_mutation_detail ALTER COLUMN fgmd_sequence_of_reference  DROP NOT NULL;