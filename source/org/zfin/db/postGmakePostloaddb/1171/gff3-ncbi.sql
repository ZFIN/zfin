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
    gff_is_in_zfin   boolean                  default false,
    gff_date_created timestamp with time zone default now()
);

/*update gff3_ncbi set gff_is_in_zfin = true where exists (
select * from db_link where
                                                     );

*/--alter table gff3_ncbi add column gff_is_in_zfin boolean default false;

CREATE SEQUENCE gff3_ncbi_seq START 100;
CREATE SEQUENCE gff3_ncbi_attribute_seq START 100;
CREATE INDEX gff3_ncbi_source_ind on gff3_ncbi (gff_source);
CREATE INDEX gff3_ncbi_ind on gff3_ncbi (gff_source);

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

create table marker_annotation_status
(
    mas_mrkr_zdb_id text,
    mas_vt_pk_id    integer
)
;

alter table marker_annotation_status
    add foreign key (mas_mrkr_zdb_id) references marker (mrkr_zdb_id);

alter table marker_annotation_status
    add foreign key (mas_vt_pk_id) references vocabulary_term (vt_id);

insert into vocabulary (v_name, v_description)
VALUES ('annotation status', 'The annotation status for genes of a given genome assembly');

insert into vocabulary_term (vt_name, vt_v_id)
VALUES ('Current', (select v_id from vocabulary where v_name = 'annotation status'));
insert into vocabulary_term (vt_name, vt_v_id)
VALUES ('Not in current annotation release', (select v_id from vocabulary where v_name = 'annotation status'));
