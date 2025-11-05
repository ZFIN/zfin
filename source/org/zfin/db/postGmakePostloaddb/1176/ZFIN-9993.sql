--liquibase formatted sql
--changeset rtaylor:ZFIN-9993

CREATE SEQUENCE load_file_log_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE TABLE load_file_log (
        "lfl_id" int8 NOT NULL DEFAULT nextval('load_file_log_id_seq'::regclass),
        "lfl_load_name" varchar(255) NOT NULL,
        "lfl_filename" varchar(255),
        "lfl_source" text,
        "lfl_date" date NOT NULL,
        "lfl_size" int8,
        "lfl_md5" varchar(32) COLLATE "pg_catalog"."default",
        "lfl_path" text COLLATE "pg_catalog"."default",
        "lfl_download_date" timestamp(6),
        "lfl_release_number" varchar(20) COLLATE "pg_catalog"."default",
        "lfl_notes" text COLLATE "pg_catalog"."default",
        "lfl_processed_date" timestamp(6)
);

ALTER TABLE load_file_log ADD CONSTRAINT "load_file_log_pkey" PRIMARY KEY ("lfl_id");
COMMENT ON TABLE load_file_log IS 'Table to track files loaded into ZFIN database';
