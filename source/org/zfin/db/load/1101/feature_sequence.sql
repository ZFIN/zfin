--liquibase formatted sql
--changeset sierra:feature_sequence.sql

create table variant_flanking_sequence (vfseq_data_zdb_id text,
                               vfseq_type text, 
                               vfseq_offset_start integer,
                               vfseq_offset_stop integer,
                               vfseq_sequence text,
                               vfseq_five_prime_flanking_sequence text,
                               vfseq_three_prime_flanking_sequence text,
                               vfseq_flanking_sequence_type text,
                               vfseq_flanking_sequence_origin text,
                               vfseq_variation text)
;

alter table variant_flanking_sequence
 add primary key (vfseq_data_zdb_id, vfseq_type);

create index vfseq_variantion_index
 on variant_flanking_sequence (vfseq_variation);

alter table variant_flanking_sequence
 add constraint variant_sequence_zdb_active_data_foreign_key
 foreign key (vfseq_data_zdb_id) references zdb_active_data(zactvd_zdb_id)
 on delete cascade;

alter table variant_flanking_sequence 
 add constraint  vfseq_flanking_sequence_type_check check (vfseq_flanking_sequence_type ='genomic' 
     or vfseq_flanking_sequence_type = 'cDNA');

alter table variant_flanking_sequence
 add constraint  vfseq_flanking_sequence_origin_check check (vfseq_flanking_sequence_origin = 'inferred from genomic'
     or vfseq_flanking_sequence_origin = 'directly sequenced' or vfseq_flanking_sequence_origin = 'unknown');

create unique index vfseq_alternate_key_index 
 on variant_flanking_sequence (vfseq_data_zdb_id, vfseq_flanking_sequence_type);

alter table variant_flanking_sequence
 add constraint vfseq_alternate_key unique using index vfseq_alternate_key_index;
