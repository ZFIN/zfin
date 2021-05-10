--liquibase formatted sql
--changeset sierra:genomic_variant.sql


create table feature_genomic_mutation_detail(fgmd_zdb_id text not null primary key,
       fgmd_feature_zdb_id text not null ,
       fgmd_sequence_of_reference text not null,
       fgmd_sequence_of_variation text not null,
       fgmd_sequence_of_reference_accession_number text,
       fgmd_variation_strand text not null)
;


create index feature_genomic_mutation_detail_feature_index
 on feature_genomic_mutation_detail (fgmd_feature_zdb_id)
;

alter table feature_genomic_mutation_detail
  add constraint fgmd_feature_fk_odc 
  foreign key (fgmd_feature_zdb_id)
  references feature(feature_zdb_id) on delete cascade;

alter table feature_genomic_mutation_detail
 add constraint fgmd_ak_constraint unique (fgmd_feature_zdb_id); 
