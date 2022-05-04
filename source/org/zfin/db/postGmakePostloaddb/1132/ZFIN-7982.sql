--liquibase formatted sql
--changeset cmpich:ZFIN-7982


create table REFERENCE_PROTEIN
(
    rp_pk_id         serial       NOT NULL,
    rp_dblink_zdb_id VARCHAR(100) NOT NULL,
    rp_gene_zdb_id   VARCHAR(100) NOT NULL,
    PRIMARY KEY (rp_pk_id),
    CONSTRAINT fk_reference_protein_dblink
        FOREIGN KEY (rp_dblink_zdb_id)
            REFERENCES DB_LINK (dblink_zdb_id),
    CONSTRAINT fk_reference_protein_marker
        FOREIGN KEY (rp_gene_zdb_id)
            REFERENCES MARKER (mrkr_zdb_id)
);