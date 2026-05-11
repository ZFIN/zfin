--liquibase formatted sql

-- B7 parent-level field additions on zirc.line_submission.
--
-- ls_single_allelic: Yes/No flag answering "Will this be submitted as
-- single-allelic?" Sits in the Background section of the edit form
-- alongside the existing maternal/paternal background fields. Nullable
-- because not-yet-answered is a real state for an in-progress draft.
--
-- ls_husbandry_info: free-text notes about husbandry-specific concerns
-- (e.g. special feeding regime). Renders as a textarea in the
-- Additional Info section.
--
-- Maternal/paternal background dropdowns from the same spec item don't
-- get a column here — they reuse the existing ls_maternal_background /
-- ls_paternal_background text columns. The UI exposes a dropdown of
-- common values plus an "Other" free-text input; whichever the user
-- picks lands in the same column. Conversion to a constrained enum is
-- deferred until the ZIRC team settles the canonical list.

--changeset rtaylor:zirc-line-submission-b7-parent-fields
ALTER TABLE zirc.line_submission
    ADD COLUMN ls_single_allelic  BOOLEAN NULL,
    ADD COLUMN ls_husbandry_info  TEXT    NULL;
