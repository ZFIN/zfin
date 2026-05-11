--liquibase formatted sql

-- Add a schema column to zdb_object_type so each home_table value is fully
-- qualified by its (schema, table) pair instead of relying on a search-path
-- lookup. Required as soon as any zfin schema other than `public` is used
-- (introduced by the ZIRC line-submission feature, where line_submission
-- lives in the `zirc` schema).
--
-- Default 'public' backfills every existing row, matching the historical
-- assumption that every zdb_object_type-tracked table lived in public.
-- This must be the first changeset in the 1182/migrations directory because
-- the redeployed p_check_zdb_object_table function (3-arg signature) reads
-- NEW.zobjtype_home_schema from row triggers; any INSERT/UPDATE on
-- zdb_object_type after the trigger redeploys but before this column exists
-- would error.
--changeset cmpich:add-zobjtype-home-schema
ALTER TABLE zdb_object_type
    ADD COLUMN IF NOT EXISTS zobjtype_home_schema TEXT NOT NULL DEFAULT 'public';
