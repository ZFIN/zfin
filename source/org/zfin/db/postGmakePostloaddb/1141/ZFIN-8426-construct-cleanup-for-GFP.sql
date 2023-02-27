--liquibase formatted sql
--changeset rtaylor:ZFIN-8426-construct-cleanup-for-GFP

-- sfGFP constructs
SELECT mrel_mrkr_1_zdb_id
INTO TEMP TABLE sfgfp_constructs
FROM marker_relationship
WHERE mrel_mrkr_2_zdb_id = 'ZDB-EFG-221115-1'
ORDER BY mrel_mrkr_1_zdb_id;

-- GFP constructs
SELECT mrel_zdb_id
INTO TEMP TABLE gfp_relations
FROM marker_relationship
WHERE mrel_mrkr_2_zdb_id = 'ZDB-EFG-070117-2'
  AND mrel_mrkr_1_zdb_id IN ( SELECT * FROM sfgfp_constructs );

-- DELETE active_data, cascade delete marker_relationships, record_attributions
DELETE FROM zdb_active_data where zactvd_zdb_id in ( SELECT mrel_zdb_id FROM gfp_relations );

