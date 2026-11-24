--liquibase formatted sql
--changeset rtaylor:ZFIN-10588-construct-cmr-repoint

-- ZFIN-10588: the 2023 changesets ZFIN-8296/8426 (GFP -> sfGFP on 25 constructs) and ZFIN-8470 (YFP -> LanYFP on 3)
-- edited marker_relationship (MR) but not construct_marker_relationship (CMR). regen_construct_marker treats CMR as the
-- source of truth, so a rename or component edit of these 28 constructs would revert the fix.
-- Do not call regen_construct_marker: once the tables match it is not needed.
--
-- The construct_component rows that link the "GFP"/"YFP" text component to the old marker are deliberately NOT
-- re-pointed. regen_construct() (the Regen-Construct_d job) treats a component whose text differs from the linked
-- marker's abbreviation as stale and rewrites the text, which would turn "sf"+"sfGFP" into "sfsfGFP". The rename/edit
-- flow also rebuilds components from the name text by abbreviation, so those links do not affect construct_marker_
-- relationship or marker_relationship. Merging sf+GFP into one sfGFP component is a curator decision.

-- 1. re-point CMR (keeps the CMREL id, so its attribution stays attached)
UPDATE construct_marker_relationship r
SET conmrkrrel_mrkr_zdb_id = m.new_mrkr
FROM (VALUES ('ZDB-EFG-070117-2', 'ZDB-EFG-221115-1'),
             ('ZDB-EFG-070409-1', 'ZDB-EFG-230215-1')) AS m(old_mrkr, new_mrkr)
WHERE r.conmrkrrel_mrkr_zdb_id = m.old_mrkr
  AND EXISTS (SELECT 1 FROM marker_relationship o
              WHERE o.mrel_mrkr_1_zdb_id = r.conmrkrrel_construct_zdb_id
                AND o.mrel_mrkr_2_zdb_id = m.new_mrkr
                AND o.mrel_type = r.conmrkrrel_relationship_type)
  AND NOT EXISTS (SELECT 1 FROM marker_relationship x
                  WHERE x.mrel_mrkr_1_zdb_id = r.conmrkrrel_construct_zdb_id
                    AND x.mrel_mrkr_2_zdb_id = r.conmrkrrel_mrkr_zdb_id
                    AND x.mrel_type = r.conmrkrrel_relationship_type);

-- 2. copy CMREL attribution to the matching MREL when it has none (must run after step 1)
INSERT INTO record_attribution (recattrib_data_zdb_id, recattrib_source_zdb_id, recattrib_source_significance, recattrib_source_type)
SELECT o.mrel_zdb_id, ra.recattrib_source_zdb_id, ra.recattrib_source_significance, ra.recattrib_source_type
FROM construct_marker_relationship r
         JOIN marker_relationship o ON o.mrel_mrkr_1_zdb_id = r.conmrkrrel_construct_zdb_id
    AND o.mrel_mrkr_2_zdb_id = r.conmrkrrel_mrkr_zdb_id AND o.mrel_type = r.conmrkrrel_relationship_type
         JOIN record_attribution ra ON ra.recattrib_data_zdb_id = r.conmrkrrel_zdb_id
WHERE r.conmrkrrel_mrkr_zdb_id IN ('ZDB-EFG-221115-1', 'ZDB-EFG-230215-1')
  AND NOT EXISTS (SELECT 1 FROM record_attribution x WHERE x.recattrib_data_zdb_id = o.mrel_zdb_id);
