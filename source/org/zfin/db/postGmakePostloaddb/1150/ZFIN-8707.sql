--liquibase formatted sql
--changeset cmpich:ZFIN-8707.sql

drop table  if exists publication_ctd;

create table publication_ctd
(
    pc_id           serial not null,
    pc_pub_zdb_id   text   not null,
    pc_ctd_id       text   not null,
    pc_date_created timestamp default CURRENT_TIMESTAMP,
    UNIQUE (pc_pub_zdb_id,pc_ctd_id)
);

ALTER TABLE publication_ctd
    add primary key (pc_id);

ALTER TABLE publication_ctd
    ADD CONSTRAINT publication_ctd_fk1
        FOREIGN KEY (pc_pub_zdb_id)
            REFERENCES publication (zdb_id);