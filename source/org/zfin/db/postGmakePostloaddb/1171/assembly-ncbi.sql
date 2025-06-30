--liquibase formatted sql
--changeset cmpich:assembly-ncbi

CREATE TABLE assembly
(
    a_pk_id          BIGINT PRIMARY KEY,
    a_name           VARCHAR(20) NOT NULL UNIQUE, -- Assembly name (eg. GRCz12tu, GRCz12ab, GRCz11, GRCz10, Zv9) (ask sridhar)
    a_gcf_identifier VARCHAR(50),
    a_order          INT-- (eg. GCF_049306965.1_GRCz12tu, etc. See https://ftp.ncbi.nlm.nih.gov/genomes/refseq/vertebrate_other/Danio_rerio/all_assembly_versions/suppressed/)
);

insert into assembly (a_pk_id, a_name, a_gcf_identifier, a_order)
values (1, 'GRCz12tu', 'GCF_049306965.1_GRCz12tu', 10),
       (3, 'GRCz11', 'GCF_000002035.6_GRCz11', 20),
       (3, 'GRCz11 or older', null, 25),
       (5, 'GRCz10', 'GCF_000002035.5_GRCz10', 30),
       (6, 'Zv9', 'GCF_000002035.4_Zv9', 40);

CREATE TABLE marker_assembly
(
    ma_mrkr_zdb_id TEXT   NOT NULL, -- FK to marker table
    ma_a_pk_id     BIGINT NOT NULL, -- FK to assembly table
    FOREIGN KEY (ma_mrkr_zdb_id) REFERENCES marker (mrkr_zdb_id),
    FOREIGN KEY (ma_a_pk_id) REFERENCES assembly (a_pk_id)
);

insert into marker_assembly (ma_mrkr_zdb_id, ma_a_pk_id)
select sfclg_data_zdb_id, 3
from sequence_feature_chromosome_location_generated
where sfclg_location_source = 'NCBIStartEndLoader'
  and exists(select * from db_link where dblink_acc_num = sfclg_acc_num)
;

