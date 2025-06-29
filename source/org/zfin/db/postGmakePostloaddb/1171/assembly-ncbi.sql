--liquibase formatted sql
--changeset cmpich:assembly-ncbi

CREATE TABLE assembly
(
    a_pk_id          BIGINT PRIMARY KEY,
    a_name           VARCHAR(20) NOT NULL UNIQUE, -- Assembly name (eg. GRCz12tu, GRCz12ab, GRCz11, GRCz10, Zv9) (ask sridhar)
    a_gcf_identifier VARCHAR(50) NOT NULL         -- (eg. GCF_049306965.1_GRCz12tu, etc. See https://ftp.ncbi.nlm.nih.gov/genomes/refseq/vertebrate_other/Danio_rerio/all_assembly_versions/suppressed/)
);

insert into assembly (a_pk_id, a_name, a_gcf_identifier)
values (1, 'GRCz12tu', 'GCF_049306965.1_GRCz12tu'),
       (3, 'GRCz11', 'GCF_000002035.6_GRCz11'),
       (4, 'GRCz10', 'GCF_000002035.5_GRCz10'),
       (5, 'Zv9', 'GCF_000002035.4_Zv9');

CREATE TABLE marker_assembly
(
    ma_mrkr_zdb_id TEXT   NOT NULL, -- FK to marker table
    ma_a_pk_id     BIGINT NOT NULL, -- FK to assembly table
    FOREIGN KEY (ma_mrkr_zdb_id) REFERENCES marker (mrkr_zdb_id),
    FOREIGN KEY (ma_a_pk_id) REFERENCES assembly (a_pk_id)
);

