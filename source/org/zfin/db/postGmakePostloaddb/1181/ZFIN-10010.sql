--liquibase formatted sql
--changeset rtaylor:ZFIN-10010

-- Clean up duplicate rows in sequence_feature_chromosome_location_generated

-- First get a list of duplicates to delete:
SELECT
    sfclg_chromosome,
    COALESCE(sfclg_data_zdb_id,        '~~NULL~~')          as sfclg_data_zdb_id,
    COALESCE(sfclg_acc_num,            '~~NULL~~')           as sfclg_acc_num,
    COALESCE(sfclg_start,              -1)                   as sfclg_start,
    COALESCE(sfclg_end,                -1)                   as sfclg_end,
    COALESCE(sfclg_location_source,    '~~NULL~~')           as sfclg_location_source,
    COALESCE(sfclg_location_subsource, '~~NULL~~')           as sfclg_location_subsource,
    COALESCE(sfclg_fdb_db_id,          -1)                   as sfclg_fdb_db_id,
    COALESCE(sfclg_pub_zdb_id,         '~~NULL~~')           as sfclg_pub_zdb_id,
    COALESCE(sfclg_assembly,           '~~NULL~~')           as sfclg_assembly,
    COALESCE(sfclg_gbrowse_track,      '~~NULL~~')           as sfclg_gbrowse_track,
    COALESCE(sfclg_evidence_code,      '~~NULL~~')           as sfclg_evidence_code,
    COALESCE(sfclg_strand,             '~')                  as sfclg_strand,
    COALESCE(sfclg_date_created,       '1970-01-01 00:00:00+00'::timestamptz) as sfclg_date_created,
    COUNT(*) as cnt
FROM sequence_feature_chromosome_location_generated
GROUP BY
    sfclg_chromosome,
    COALESCE(sfclg_data_zdb_id,        '~~NULL~~'),
    COALESCE(sfclg_acc_num,            '~~NULL~~'),
    COALESCE(sfclg_start,              -1),
    COALESCE(sfclg_end,                -1),
    COALESCE(sfclg_location_source,    '~~NULL~~'),
    COALESCE(sfclg_location_subsource, '~~NULL~~'),
    COALESCE(sfclg_fdb_db_id,          -1),
    COALESCE(sfclg_pub_zdb_id,         '~~NULL~~'),
    COALESCE(sfclg_assembly,           '~~NULL~~'),
    COALESCE(sfclg_gbrowse_track,      '~~NULL~~'),
    COALESCE(sfclg_evidence_code,      '~~NULL~~'),
    COALESCE(sfclg_strand,             '~'),
    COALESCE(sfclg_date_created,       '1970-01-01 00:00:00+00'::timestamptz)
HAVING COUNT(*) > 1
ORDER BY cnt DESC;


-- Uses placeholders for NULL values so that duplicates with nulls will also get cleaned up. 
-- Confirmed that the DB doesn't container '~~NULL~~', -1, '~', '1970-01-01 00:00:00+00' in the relevant fields
DELETE FROM sequence_feature_chromosome_location_generated
WHERE sfclg_pk_id IN (
    SELECT sfclg_pk_id
    FROM (
        SELECT sfclg_pk_id,
               ROW_NUMBER() OVER (
                   PARTITION BY
                       sfclg_chromosome,
                       COALESCE(sfclg_data_zdb_id,        '~~NULL~~'),
                       COALESCE(sfclg_acc_num,            '~~NULL~~'),
                       COALESCE(sfclg_start,              -1),
                       COALESCE(sfclg_end,                -1),
                       COALESCE(sfclg_location_source,    '~~NULL~~'),
                       COALESCE(sfclg_location_subsource, '~~NULL~~'),
                       COALESCE(sfclg_fdb_db_id,          -1),
                       COALESCE(sfclg_pub_zdb_id,         '~~NULL~~'),
                       COALESCE(sfclg_assembly,           '~~NULL~~'),
                       COALESCE(sfclg_gbrowse_track,      '~~NULL~~'),
                       COALESCE(sfclg_evidence_code,      '~~NULL~~'),
                       COALESCE(sfclg_strand,             '~'),
                       COALESCE(sfclg_date_created,       '1970-01-01 00:00:00+00'::timestamptz)
                   ORDER BY sfclg_pk_id
               ) AS rn
        FROM sequence_feature_chromosome_location_generated
    ) dupes
    WHERE rn > 1
);

-- Add a constraint so we can't add duplicates in the future
ALTER TABLE sequence_feature_chromosome_location_generated
ADD CONSTRAINT uq_sfclg_unique_location
UNIQUE NULLS NOT DISTINCT (
    sfclg_chromosome,
    sfclg_data_zdb_id,
    sfclg_acc_num,
    sfclg_start,
    sfclg_end,
    sfclg_location_source,
    sfclg_location_subsource,
    sfclg_fdb_db_id,
    sfclg_pub_zdb_id,
    sfclg_assembly,
    sfclg_gbrowse_track,
    sfclg_evidence_code,
    sfclg_strand,
    sfclg_date_created
);


