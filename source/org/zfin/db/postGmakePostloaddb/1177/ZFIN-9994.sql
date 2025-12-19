--liquibase formatted sql
--changeset rtaylor:ZFIN-9994

CREATE SEQUENCE zdb_property_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE TABLE zdb_property (
        "zprop_id" int8 NOT NULL DEFAULT nextval('zdb_property_id_seq'::regclass),
        "zprop_name" varchar(255) NOT NULL,
        "zprop_value" text,
        "zprop_type" varchar(255)
);

ALTER TABLE zdb_property ADD CONSTRAINT "zdb_property_pkey" PRIMARY KEY ("zprop_id");

-- unique constraint on (name) to prevent duplicate entries
ALTER TABLE zdb_property ADD CONSTRAINT "zdb_property_name_key" UNIQUE ("zprop_name");

COMMENT ON TABLE zdb_property IS 'Table to store "property" like values that are key-value pairs more suitable to DB storage than config files';
