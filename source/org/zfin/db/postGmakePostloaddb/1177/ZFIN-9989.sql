--liquibase formatted sql
--changeset cmpich:ZFIN-9998

-- Create comprehensive unique constraint to prevent duplicate location records                                                                                                                                                                                                                    │
ALTER TABLE sequence_feature_chromosome_location_generated
    ADD CONSTRAINT uk_sfclg_unique_location
        UNIQUE (sfclg_data_zdb_id, sfclg_location_source, sfclg_assembly, sfclg_location_subsource, sfclg_acc_num, sfclg_start, sfclg_end);

-- remove duplicated records
DELETE
FROM sequence_feature_chromosome_location_generated
WHERE sfclg_pk_id IN (SELECT sfclg_pk_id
                      FROM (SELECT sfclg_pk_id,
                                   ROW_NUMBER() OVER (
                                       PARTITION BY sfclg_data_zdb_id, sfclg_chromosome, sfclg_start, sfclg_end, sfclg_acc_num
                                       ORDER BY sfclg_pk_id
                                       ) as row_num
                            FROM sequence_feature_chromosome_location_generated
                            WHERE sfclg_location_source = 'ZFIN') ranked
                      WHERE row_num > 1);
