--liquibase formatted sql
--changeset rtaylor:ZFIN-9966

-- Drop the existing foreign key constraint
ALTER TABLE marker_annotation_status
DROP CONSTRAINT marker_annotation_status_mas_mrkr_zdb_id_fkey;

-- Add the constraint back with ON DELETE CASCADE
ALTER TABLE marker_annotation_status
    ADD CONSTRAINT marker_annotation_status_mas_mrkr_zdb_id_fkey
        FOREIGN KEY (mas_mrkr_zdb_id)
            REFERENCES marker(mrkr_zdb_id)
            ON DELETE CASCADE;

