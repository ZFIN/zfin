--liquibase formatted sql
--changeset sierra:feature_sequence.sql

create table variant_sequence (vseq_data_zdb_id text,
                               vseq_type text, 
                               vseq_offset_start integer,
                               vseq_offset_stop integer,
                               vseq_sequence text,
                               vseq_five_prime_end text,
                               vseq_three_prime_end text,
                               vseq_variation text)
;

alter table variant_sequence
 add primary key (vseq_data_zdb_id, vseq_type);

create index vseq_variantion_index
 on variant_sequence (vseq_variation);

alter table variant_sequence
  add constraint variant_sequence_variation_foreign_key 
  foreign key (vseq_variation) references sequence_ambiguity_code(seqac_meaning)
  on update restrict on delete restrict;

alter table variant_sequence
 add constraint variant_sequence_zdb_active_data_foreign_key
 foreign key (vseq_data_zdb_id) references zdb_active_data(zactvd_zdb_id)
 on delete cascade;

insert into variant_sequence
  select * from snp_sequence;

drop table snp_sequence;
