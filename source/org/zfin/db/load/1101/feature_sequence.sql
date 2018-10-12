--liquibase formatted sql
--changeset sierra:feature_sequence.sql

create table variant_sequence (vseq_data_zdb_id text,
                               vseq_type text, 
                               vseq_offset_start integer,
                               vseq_offset_stop integer,
                               vseq_sequence text,
                               vseq_five_prime_flanking_sequence text,
                               vseq_three_prime_flanking_sequence text,
                               vseq_flanking_sequence_type text,
                               vseq_flanking_sequence_origin text,
                               vseq_variation text)
;

alter table variant_sequence
 add primary key (vseq_data_zdb_id, vseq_type);

create index vseq_variantion_index
 on variant_sequence (vseq_variation);

alter table variant_sequence
 add constraint variant_sequence_zdb_active_data_foreign_key
 foreign key (vseq_data_zdb_id) references zdb_active_data(zactvd_zdb_id)
 on delete cascade;

alter table variant_sequence 
 add constraint check vseq_flanking_sequence_type_check vseq_flanking_sequence_type in ('genomic','cDNA');

alter table variant_sequence
 add constraint check vseq_flanking_sequence_origin_check vseq_flanking_sequence_origin in ('inferred from genomic','directly sequenced', 'unknown');

create unique index vseq_alternate_key_index 
 on variant_sequence (vseq_data_zdb_id, vseq_flanking_sequence_type);

alter table variant_sequence
 add constraint vseq_alternate_key unique using index vseq_alternate_key_index;
