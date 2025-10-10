--liquibase formatted sql
--changeset rtaylor:ZFIN-9966b

-- Drop the existing foreign key constraint
-- And add the constraint back with ON DELETE CASCADE
ALTER TABLE marker_assembly
DROP CONSTRAINT marker_assembly_ma_mrkr_zdb_id_fkey;

ALTER TABLE marker_assembly
    ADD CONSTRAINT marker_assembly_ma_mrkr_zdb_id_fkey
        FOREIGN KEY (ma_mrkr_zdb_id)
            REFERENCES marker(mrkr_zdb_id)
            ON DELETE CASCADE;