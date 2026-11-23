--liquibase formatted sql
--changeset rtaylor:ZFIN-10588-construct-cmr-repoint

-- ZFIN-10588: the 2023 changesets ZFIN-8296/8426 (GFP -> sfGFP on 25 constructs) and ZFIN-8470 (YFP -> LanYFP on 3)
-- edited marker_relationship (MR) but not construct_marker_relationship (CMR). regen_construct_marker treats CMR as the
-- source of truth, so a rename or component edit of these 28 constructs would revert the fix.
-- Do not call regen_construct_marker: once the tables match it is not needed.
-- construct_component_trigger fires update_construct_name on the component updates; names come from cc_component
-- text, not the linked marker, so construct names do not change.

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

-- 3. components still linked to GFP/YFP on constructs whose CMR now points at sfGFP/LanYFP.
-- The sf+GFP / lan+YFP text-plus-marker pairs are deliberately not merged (curator decision).
UPDATE construct_component SET cc_component_zdb_id = 'ZDB-EFG-221115-1'
WHERE cc_component_zdb_id = 'ZDB-EFG-070117-2'
  AND cc_construct_zdb_id IN (SELECT conmrkrrel_construct_zdb_id FROM construct_marker_relationship WHERE conmrkrrel_mrkr_zdb_id = 'ZDB-EFG-221115-1')
  -- guard: leave constructs that legitimately still have the old marker (e.g. two cassettes, sfGFP and GFP)
  AND NOT EXISTS (SELECT 1 FROM construct_marker_relationship o WHERE o.conmrkrrel_construct_zdb_id = cc_construct_zdb_id AND o.conmrkrrel_mrkr_zdb_id = 'ZDB-EFG-070117-2');
UPDATE construct_component SET cc_component_zdb_id = 'ZDB-EFG-230215-1'
WHERE cc_component_zdb_id = 'ZDB-EFG-070409-1'
  AND cc_construct_zdb_id IN (SELECT conmrkrrel_construct_zdb_id FROM construct_marker_relationship WHERE conmrkrrel_mrkr_zdb_id = 'ZDB-EFG-230215-1')
  -- guard: leave constructs that legitimately still have the old marker (e.g. two cassettes, sfGFP and GFP)
  AND NOT EXISTS (SELECT 1 FROM construct_marker_relationship o WHERE o.conmrkrrel_construct_zdb_id = cc_construct_zdb_id AND o.conmrkrrel_mrkr_zdb_id = 'ZDB-EFG-070409-1');
