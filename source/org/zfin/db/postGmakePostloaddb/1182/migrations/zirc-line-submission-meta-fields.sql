--liquibase formatted sql

-- Meta fields on zirc.line_submission: draft/submitted/soft-delete state plus an
-- updated_at trigger. Pairs with the create-on-first-save flow (new submissions
-- start as drafts) and prepares the ground for future review workflow.

--changeset rtaylor:zirc-line-submission-meta-fields
ALTER TABLE zirc.line_submission
    ADD COLUMN ls_is_draft     BOOLEAN   NOT NULL DEFAULT TRUE,
    ADD COLUMN ls_deleted_at   TIMESTAMP NULL,
    ADD COLUMN ls_submitted_at TIMESTAMP NULL;

CREATE OR REPLACE FUNCTION zirc.line_submission_auto_timestamp()
RETURNS TRIGGER AS '
BEGIN
    NEW.ls_updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
' LANGUAGE plpgsql;

CREATE TRIGGER trg_line_submission_auto_timestamp
BEFORE UPDATE ON zirc.line_submission
FOR EACH ROW
EXECUTE FUNCTION zirc.line_submission_auto_timestamp();
