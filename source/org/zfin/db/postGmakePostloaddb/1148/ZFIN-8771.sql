--liquibase formatted sql
--changeset rtaylor:ZFIN-8771.sql
DROP TABLE IF EXISTS uniprot_release;
CREATE TABLE uniprot_release (
    upr_id bigserial NOT NULL,
    upr_date date NOT NULL,
    upr_size bigint,
    upr_md5 character varying(32),
    upr_path text,
    upr_download_date timestamp without time zone,
    upr_release_number character varying(20),
    upr_notes text,
    upr_processed_date timestamp without time zone,
    CONSTRAINT uniprot_release_pkey PRIMARY KEY (upr_id)
);

ALTER TABLE uniprot_release OWNER TO informix;

INSERT INTO uniprot_release (upr_date, upr_size, upr_md5, upr_path)
    VALUES ('2023-05-03', 319230408, '266ca58c9512c50e51f72b96c1ba31a1', '2023-05/pre_zfin.dat');


