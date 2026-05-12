--liquibase formatted sql

-- Replace the zirc.line_submission_reason junction table with two columns on
-- the parent: ls_reasons (TEXT[] of canonical snake_case option values) plus
-- ls_reasons_other (single free-text "Other" entry, NULL when the user didn't
-- pick the Other checkbox). Matches the form spec — a CHECKBOX_GROUP with
-- allowOther becomes a multi-select plus one optional text field — and avoids
-- a join for read-mostly data that's always loaded with its parent submission.
--
-- Pattern follows ui.zebrafish_models_chebi_association.omca_ancestor_term_ids
-- (see source/org/zfin/db/postGmakePostloaddb/1181/add-ancestor-term-ids.sql).

--changeset rtaylor:zirc-line-submission-reasons-array
ALTER TABLE zirc.line_submission
    ADD COLUMN ls_reasons        TEXT[] NOT NULL DEFAULT '{}',
    ADD COLUMN ls_reasons_other  TEXT   NULL;

DROP TABLE zirc.line_submission_reason;
