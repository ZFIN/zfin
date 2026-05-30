--liquibase formatted sql

-- Convert ls_previous_names from a single VARCHAR(255) to a TEXT[] so the
-- React form can edit Previous Names as a list (one entry per row in the
-- stringList widget) instead of a single comma-separated free-text field.
--
-- No backfill: the existing column has only smoke-test data, and parsing
-- "foo, bar" out of free text is ambiguous (curators used different
-- separators). The new column defaults to an empty array.

--changeset zirc:zirc-previous-names-array
ALTER TABLE zirc.line_submission DROP COLUMN ls_previous_names;
ALTER TABLE zirc.line_submission
    ADD COLUMN ls_previous_names TEXT[] NOT NULL DEFAULT '{}';
