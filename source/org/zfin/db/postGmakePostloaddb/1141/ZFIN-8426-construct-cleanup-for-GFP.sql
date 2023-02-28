--liquibase formatted sql
--changeset rtaylor:ZFIN-8426-construct-cleanup-for-GFP

-- DELETE active_data, cascade delete marker_relationships, record_attributions
DELETE FROM zdb_active_data where zactvd_zdb_id in (
    -- mr1 is sfGFP construct relationships, mr2 is GFP construct relationships
    SELECT mr2.mrel_zdb_id
    FROM marker_relationship mr1 JOIN marker_relationship mr2
                                 ON mr1.mrel_mrkr_1_zdb_id = mr2.mrel_mrkr_1_zdb_id
    WHERE mr1.mrel_mrkr_2_zdb_id = 'ZDB-EFG-221115-1'
      AND mr2.mrel_mrkr_2_zdb_id = 'ZDB-EFG-070117-2');
