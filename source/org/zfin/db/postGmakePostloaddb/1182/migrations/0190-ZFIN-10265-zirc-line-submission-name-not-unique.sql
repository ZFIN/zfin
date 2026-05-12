--liquibase formatted sql

-- Drop the unique constraint on zirc.line_submission.ls_name. The original
-- 1182/migrations/0050 migration left the field UNIQUE on the assumption
-- that submission names are globally distinguishable; in practice curators
-- legitimately submit different lines with the same name (and the
-- constraint surfaces as a 500 mid-typing whenever a duplicate would
-- result, which is hostile autosave UX).
--
--changeset rtaylor:zirc-line-submission-name-not-unique
ALTER TABLE zirc.line_submission DROP CONSTRAINT IF EXISTS line_submission_name_unique;
