--liquibase formatted sql
--changeset rtaylor:ZFIN-8233-pre

CREATE TABLE thisse.clon_class14 (
    "count" text,
    "vector" text,
    "box" text,
    "clone_name" text,
    "alternative_name" text,
    "size" text,
    "comments" text,
    "cloning_site" text,
    "antisense" text,
    "sense" text,
    "clone_class" text,
    sample_type text
);

