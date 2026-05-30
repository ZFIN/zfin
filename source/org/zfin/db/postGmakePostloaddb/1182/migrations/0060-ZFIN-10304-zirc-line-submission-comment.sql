--liquibase formatted sql

-- Curator/submitter conversation thread attached to a line submission's
-- fields and sections. Each row is one comment. The (rec_id, scope,
-- field_name|section_name) tuple identifies the thread it belongs to,
-- reusing the same rec_id scheme already used by the audit log:
--     ZDB-LINESUBMISSION-…   top-level submission
--     ZIRC-MUT-<id>          mutation
--     ZIRC-GENE-<id>         gene
--     ZIRC-LESION-<id>       lesion
--     ZIRC-GA-<id>           genotyping assay
--     ZIRC-PHEN-<id>         phenotype
--     ZIRC-LF-<a>-<b>        linked feature pair
--
-- Field-scope comments fill lsc_field_name; section-scope comments fill
-- lsc_section_name. A CHECK constraint enforces exactly one of the two
-- is non-null and matches lsc_scope.

--changeset cmpich:zirc-line-submission-comment-schema
CREATE TABLE zirc.line_submission_comment (
    lsc_pk_id        BIGSERIAL    PRIMARY KEY,
    lsc_rec_id       TEXT         NOT NULL,
    lsc_scope        TEXT         NOT NULL
                                  CONSTRAINT lsc_scope_check
                                  CHECK (lsc_scope IN ('field', 'section')),
    lsc_field_name   TEXT,
    lsc_section_name TEXT,
    lsc_author_zdb_id TEXT        NOT NULL
                                  CONSTRAINT lsc_author_fk
                                  REFERENCES person(zdb_id)
                                  ON UPDATE RESTRICT ON DELETE RESTRICT,
    lsc_comment      TEXT         NOT NULL,
    lsc_created_at   TIMESTAMP(3) NOT NULL DEFAULT now(),
    CONSTRAINT lsc_scope_target_check CHECK (
        (lsc_scope = 'field'   AND lsc_field_name   IS NOT NULL AND lsc_section_name IS NULL)
     OR (lsc_scope = 'section' AND lsc_section_name IS NOT NULL AND lsc_field_name   IS NULL)
    )
);

CREATE INDEX line_submission_comment_field_idx
    ON zirc.line_submission_comment (lsc_rec_id, lsc_field_name)
 WHERE lsc_scope = 'field';

CREATE INDEX line_submission_comment_section_idx
    ON zirc.line_submission_comment (lsc_rec_id, lsc_section_name)
 WHERE lsc_scope = 'section';

--changeset cmpich:zirc-line-submission-comment-closed-flag
-- Adds the closed flag the entity declares. Separate changeset so existing
-- dev DBs that already ran the create-table changeset pick this up without
-- a checksum conflict.
ALTER TABLE zirc.line_submission_comment
    ADD COLUMN IF NOT EXISTS lsc_closed BOOLEAN NOT NULL DEFAULT false;
