--liquibase formatted sql
--changeset sierra:add_key_for_variant_flanking_sequence

alter table variant_flanking_sequence
 add vfseq_zdb_id text;

alter table variant_flanking_sequence
  drop constraint variant_flanking_sequence_pkey;

alter table variant_flanking_sequence
  add primary key (vfseq_zdb_id);

