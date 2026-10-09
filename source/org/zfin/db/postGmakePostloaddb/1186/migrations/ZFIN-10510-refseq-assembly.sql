--liquibase formatted sql
--changeset rtaylor:ZFIN-10510-refseq-assembly

insert into assembly (a_pk_id, a_name, a_gcf_identifier, a_order)
values (2, 'GRCz13ab', 'GCF_052040795.2_GRCz13ab', 5),
       (7, 'GRCz12ab', 'GCF_052040795.1_GRCz12ab', 7);

CREATE TABLE db_link_assembly
(
    dbla_dblink_zdb_id TEXT   NOT NULL, -- FK to db_link table
    dbla_a_pk_id        BIGINT NOT NULL, -- FK to assembly table
    PRIMARY KEY (dbla_dblink_zdb_id, dbla_a_pk_id),
    FOREIGN KEY (dbla_dblink_zdb_id) REFERENCES db_link (dblink_zdb_id) ON DELETE CASCADE,
    FOREIGN KEY (dbla_a_pk_id) REFERENCES assembly (a_pk_id)
);
