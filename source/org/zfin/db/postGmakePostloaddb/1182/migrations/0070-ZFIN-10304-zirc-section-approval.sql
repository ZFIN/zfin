--liquibase formatted sql

-- Per-section curator-approval flag for a line submission. One row per
-- (rec_id, section_name) tuple. rec_id reuses the audit-log identifier
-- scheme (ZDB-LINESUBMISSION-…, ZIRC-MUT-N, …) so we can approve at
-- both the submission level and the per-mutation inner sections.
--
-- Toggling the flag also writes a row to the updates table (server-side)
-- so the history popup picks it up alongside other field changes.

--changeset cmpich:zirc-line-submission-section-approval-schema
CREATE TABLE zirc.line_submission_section_approval (
    lssa_pk_id           BIGSERIAL    PRIMARY KEY,
    lssa_rec_id          TEXT         NOT NULL,
    lssa_section_name    TEXT         NOT NULL,
    lssa_approved        BOOLEAN      NOT NULL DEFAULT FALSE,
    lssa_approver_zdb_id TEXT         NOT NULL
                                      CONSTRAINT lssa_approver_fk
                                      REFERENCES person(zdb_id)
                                      ON UPDATE RESTRICT ON DELETE RESTRICT,
    lssa_approved_at     TIMESTAMP(3) NOT NULL DEFAULT now(),
    CONSTRAINT lssa_rec_section_unique UNIQUE (lssa_rec_id, lssa_section_name)
);

CREATE INDEX line_submission_section_approval_rec_idx
    ON zirc.line_submission_section_approval (lssa_rec_id);
