--liquibase formatted sql

-- ZFIN-10325: ZIRC submission form supports PI alongside submitter.
-- ─────────────────────────────────────────────────────────────────────────
-- The original line_submission_person PK was (submission, person, role) —
-- a person could legitimately hold multiple roles on the same submission.
-- Product decision: at most one role per person per submission. Enforce
-- via a UNIQUE constraint on (submission, person) — keeps the existing
-- PK intact (Hibernate's @IdClass still resolves entity identity) while
-- the new constraint rejects a second row for the same (submission,
-- person) pair regardless of role.
--
-- Also add a CHECK that lsp_role is one of the known wire values, so a
-- future code path can't smuggle a typoed role string into the column.
-- Admin users are not modeled here yet (hard-coded list in the React
-- Status Overview bar for now); when they become tagged-per-submission
-- a follow-up migration will extend the CHECK to include 'admin'.

--changeset cmpich:ZFIN-10325-lsp-one-role-per-person
ALTER TABLE zirc.line_submission_person
    ADD CONSTRAINT lsp_one_role_per_person
    UNIQUE (lsp_line_submission_id, lsp_person_zdb_id);

ALTER TABLE zirc.line_submission_person
    ADD CONSTRAINT lsp_role_chk
    CHECK (lsp_role IN ('submitter', 'pi'));
