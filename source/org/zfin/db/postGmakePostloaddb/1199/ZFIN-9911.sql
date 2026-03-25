--liquibase formatted sql
--changeset cmpich:ZFIN-9911

-- ZFIN-9911: Fix duplicated feature location rows on mapping detail pages
--
-- 1. Backfill citations on 'other map location' feature rows that lack one
-- 2. Remove duplicate rows (same feature+chr+start+end+assembly+source), keeping one per group

-- Step 1: Backfill citation on feature location rows that lack one
UPDATE sequence_feature_chromosome_location_generated
SET sfclg_pub_zdb_id = 'ZDB-PUB-260306-1'
WHERE sfclg_data_zdb_id LIKE 'ZDB-ALT-%'
  AND sfclg_location_source = 'other map location'
  AND sfclg_pub_zdb_id IS NULL;

-- Step 2: Remove duplicate 'other map location' rows
-- Keep one row per (feature, chromosome, start, end, assembly, source) group.
-- Among duplicates, keep the row with the lowest PK id.
DELETE FROM sequence_feature_chromosome_location_generated
WHERE sfclg_pk_id IN (
    SELECT sfclg_pk_id FROM (
        SELECT sfclg_pk_id,
               row_number() OVER (
                   PARTITION BY sfclg_data_zdb_id, sfclg_chromosome,
                                sfclg_start, sfclg_end,
                                sfclg_assembly, sfclg_location_source
                   ORDER BY sfclg_pk_id
               ) AS rn
        FROM sequence_feature_chromosome_location_generated
        WHERE sfclg_data_zdb_id LIKE 'ZDB-ALT-%'
          AND sfclg_location_source = 'other map location'
    ) ranked
    WHERE rn > 1
);