--liquibase formatted sql
--changeset cmpich:ZFIN-8607.sql

CREATE SEQUENCE ZEBRAFISH_MODELS_DISPLAY_seq START 1;
CREATE SEQUENCE publication_expression_display_seq START 1;
CREATE SEQUENCE omim_display_display_seq START 1;


ALTER TABLE UI.CHEBI_PHENOTYPE_WAREHOUSE_ASSOCIATION
    ADD COLUMN created_at TIMESTAMP DEFAULT NOW();

ALTER TABLE UI.OMIM_ZFIN_ASSOCIATION
    ADD COLUMN created_at TIMESTAMP DEFAULT NOW();

ALTER TABLE UI.OMIM_PHENOTYPE_DISPLAY
    ADD COLUMN created_at TIMESTAMP DEFAULT NOW();

ALTER TABLE UI.ZEBRAFISH_MODELS_EVIDENCE_ASSOCIATION
    ADD COLUMN created_at TIMESTAMP DEFAULT NOW();

ALTER TABLE UI.ZEBRAFISH_MODELS_DISPLAY
    ADD COLUMN created_at TIMESTAMP DEFAULT NOW();

ALTER TABLE UI.PUBLICATION_EXPRESSION_DISPLAY
    ADD COLUMN created_at TIMESTAMP DEFAULT NOW();

ALTER TABLE UI.TERM_PHENOTYPE_DISPLAY
    ADD COLUMN created_at TIMESTAMP DEFAULT NOW();

ALTER TABLE UI.PHENOTYPE_WAREHOUSE_ASSOCIATION
    ADD COLUMN created_at TIMESTAMP DEFAULT NOW();

ALTER TABLE UI.PHENOTYPE_ZFIN_ASSOCIATION
    ADD COLUMN created_at TIMESTAMP DEFAULT NOW();

ALTER TABLE ui.zebrafish_models_chebi_association
    ADD COLUMN created_at TIMESTAMP DEFAULT NOW();

ALTER TABLE UI.ZEBRAFISH_MODELS_DISPLAY
    ADD COLUMN zmd_fishox_zdb_id text;

drop table if exists UI.INDEXER_RUN;
create table UI.INDEXER_RUN
(
    ir_id         serial8 not null,
    ir_start_date TIMESTAMP,
    ir_end_date   TIMESTAMP,
    ir_duration   integer
);

drop table if exists UI.INDEXER_INFO;
create table UI.INDEXER_INFO
(
    ii_id         serial8 not null,
    ii_ir_id      integer,
    ii_name       text,
    ii_start_date TIMESTAMP,
    ii_duration   integer,
    ii_count      integer
);

drop table if exists UI.INDEXER_TASK;
create table UI.INDEXER_TASK
(
    it_id         serial8 not null,
    it_ii_id      integer,
    it_name       text,
    it_start_date TIMESTAMP,
    it_duration   integer
);

