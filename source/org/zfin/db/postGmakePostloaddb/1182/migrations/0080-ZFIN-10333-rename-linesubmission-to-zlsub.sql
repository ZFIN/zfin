--liquibase formatted sql

-- Rename the ZIRC line-submission ZDB-ID type from LINESUBMISSION to
-- ZLSUB. This is a full code+data rename:
--   * zdb_object_type row is relabelled
--   * the per-type sequence linesubmission_seq is renamed to zlsub_seq
--   * every existing ZDB-LINESUBMISSION-* identifier (PK rows in
--     zdb_active_data and zirc.line_submission, FK columns in the rest
--     of the submission tree, and free-form rec_id columns on the
--     audit / comment / approval tables) is rewritten to ZDB-ZLSUB-*.
--
-- The FK constraints from the line_submission tree to
-- zirc.line_submission(ls_zdb_id) are dropped before the UPDATEs and
-- re-added afterward; their NO ACTION / RESTRICT defaults would
-- otherwise refuse the PK rename.

--changeset cmpich:zlsub-rename-fk-drop
ALTER TABLE zirc.line_submission                  DROP CONSTRAINT IF EXISTS line_submission_zdb_id_active_data_fk;
ALTER TABLE zirc.line_submission_person           DROP CONSTRAINT IF EXISTS fk_line_submission_person_submission;
ALTER TABLE zirc.mutation                         DROP CONSTRAINT IF EXISTS fk_mutation_line_submission;
ALTER TABLE zirc.line_submission_linked_feature   DROP CONSTRAINT IF EXISTS fk_line_submission_linked_feature_parent;

--changeset cmpich:zlsub-rename-zdb-object-type
UPDATE zdb_object_type SET zobjtype_name = 'ZLSUB' WHERE zobjtype_name = 'LINESUBMISSION';

--changeset cmpich:zlsub-rename-sequence
ALTER SEQUENCE IF EXISTS linesubmission_seq RENAME TO zlsub_seq;

--changeset cmpich:zlsub-rename-active-data
UPDATE zdb_active_data
   SET zactvd_zdb_id = REPLACE(zactvd_zdb_id, 'ZDB-LINESUBMISSION-', 'ZDB-ZLSUB-')
 WHERE zactvd_zdb_id LIKE 'ZDB-LINESUBMISSION-%';

--changeset cmpich:zlsub-rename-line-submission-pk
UPDATE zirc.line_submission
   SET ls_zdb_id = REPLACE(ls_zdb_id, 'ZDB-LINESUBMISSION-', 'ZDB-ZLSUB-')
 WHERE ls_zdb_id LIKE 'ZDB-LINESUBMISSION-%';

--changeset cmpich:zlsub-rename-line-submission-children
UPDATE zirc.line_submission_person
   SET lsp_line_submission_id = REPLACE(lsp_line_submission_id, 'ZDB-LINESUBMISSION-', 'ZDB-ZLSUB-')
 WHERE lsp_line_submission_id LIKE 'ZDB-LINESUBMISSION-%';

UPDATE zirc.mutation
   SET m_line_submission_id = REPLACE(m_line_submission_id, 'ZDB-LINESUBMISSION-', 'ZDB-ZLSUB-')
 WHERE m_line_submission_id LIKE 'ZDB-LINESUBMISSION-%';

UPDATE zirc.line_submission_linked_feature
   SET lslf_line_submission_id = REPLACE(lslf_line_submission_id, 'ZDB-LINESUBMISSION-', 'ZDB-ZLSUB-')
 WHERE lslf_line_submission_id LIKE 'ZDB-LINESUBMISSION-%';

--changeset cmpich:zlsub-rename-audit-and-thread-rec-ids splitStatements:false
UPDATE updates
   SET rec_id = REPLACE(rec_id, 'ZDB-LINESUBMISSION-', 'ZDB-ZLSUB-')
 WHERE rec_id LIKE 'ZDB-LINESUBMISSION-%';

-- The zirc.line_submission_{comment,section_approval} tables come from
-- the separate ZFIN-10304 feature and may not have landed in this
-- environment yet, so guard the rec_id rewrites with to_regclass.
DO $$
BEGIN
    IF to_regclass('zirc.line_submission_comment') IS NOT NULL THEN
        UPDATE zirc.line_submission_comment
           SET lsc_rec_id = REPLACE(lsc_rec_id, 'ZDB-LINESUBMISSION-', 'ZDB-ZLSUB-')
         WHERE lsc_rec_id LIKE 'ZDB-LINESUBMISSION-%';
    END IF;
    IF to_regclass('zirc.line_submission_section_approval') IS NOT NULL THEN
        UPDATE zirc.line_submission_section_approval
           SET lssa_rec_id = REPLACE(lssa_rec_id, 'ZDB-LINESUBMISSION-', 'ZDB-ZLSUB-')
         WHERE lssa_rec_id LIKE 'ZDB-LINESUBMISSION-%';
    END IF;
END $$;

--changeset cmpich:zlsub-rename-fk-restore
ALTER TABLE zirc.line_submission
    ADD CONSTRAINT line_submission_zdb_id_active_data_fk
    FOREIGN KEY (ls_zdb_id) REFERENCES zdb_active_data(zactvd_zdb_id)
    ON UPDATE RESTRICT ON DELETE CASCADE;

ALTER TABLE zirc.line_submission_person
    ADD CONSTRAINT fk_line_submission_person_submission
    FOREIGN KEY (lsp_line_submission_id) REFERENCES zirc.line_submission(ls_zdb_id)
    ON DELETE CASCADE;

ALTER TABLE zirc.mutation
    ADD CONSTRAINT fk_mutation_line_submission
    FOREIGN KEY (m_line_submission_id) REFERENCES zirc.line_submission(ls_zdb_id)
    ON DELETE CASCADE;

ALTER TABLE zirc.line_submission_linked_feature
    ADD CONSTRAINT fk_line_submission_linked_feature_parent
    FOREIGN KEY (lslf_line_submission_id) REFERENCES zirc.line_submission(ls_zdb_id)
    ON DELETE CASCADE;
