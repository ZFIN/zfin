--liquibase formatted sql
--changeset cmpich:gff3_ncbi_pre

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


create table temp_gff3_ncbi
(
    gff_pk_id      serial,
    gff_seqname    text not null,
    gff_source     text,
    gff_feature    text,
    gff_start      int,
    gff_end        int,
    gff_score      text,
    gff_strand     char(1),
    gff_frame      char(1),
    gff_attributes text,
    gff_start_s    text,
    gff_end_s      text
);

