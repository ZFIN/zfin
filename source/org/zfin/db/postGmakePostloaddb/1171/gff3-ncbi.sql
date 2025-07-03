--liquibase formatted sql
--changeset cmpich:gff3_ncbi

create table gff3_ncbi
(
    gff_pk_id        serial,
    gff_seqname      text not null,
    gff_source       text,
    gff_feature      text,
    gff_start        int,
    gff_end          int,
    gff_score        text,
    gff_strand       text,
    gff_frame        text,
    gff_attributes   text,
    gff_date_created timestamp with time zone default now()
);

CREATE SEQUENCE gff3_ncbi_seq START 100;
CREATE SEQUENCE gff3_ncbi_attribute_seq START 100;

create table gff3_ncbi_attribute
(
    gna_pk_id     serial,
    gna_gff_pk_id serial,
    gna_key       text,
    gna_value     text
);

alter table sequence_feature_chromosome_location_generated
    add foreign key (sfclg_fdb_db_id) references foreign_db (fdb_db_pk_id);

-- cannot create this as there are many accessions with version numbers on it on the sequence_feature_chromosome_location_generated table
-- where db_link table does not contain versioned accessions.

