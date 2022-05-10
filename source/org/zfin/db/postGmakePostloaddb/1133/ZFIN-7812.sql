--liquibase formatted sql
--changeset rtaylor:ZFIN-7812


DROP TABLE IF EXISTS "public"."clone_agp_grcz11";
CREATE TABLE "public"."clone_agp_grcz11" (
  "cag_pk_id" SERIAL PRIMARY KEY,
  "chromosome" varchar(255) COLLATE "pg_catalog"."default",
  "object_start" int8,
  "object_end" int8,
  "ix" varchar(8),
  "component_type" varchar(8) COLLATE "pg_catalog"."default",
  "component_id" varchar(255) COLLATE "pg_catalog"."default",
  "component_start" int8,
  "component_end" int8,
  "strand" varchar(8) COLLATE "pg_catalog"."default",
  "gap_length" int8,
  "gap_type" varchar(255) COLLATE "pg_catalog"."default",
  "linkage" bool,
  "linkage_evidence" varchar(255) COLLATE "pg_catalog"."default"
);
