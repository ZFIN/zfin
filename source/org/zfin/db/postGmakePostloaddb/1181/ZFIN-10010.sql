--liquibase formatted sql
--changeset rtaylor:ZFIN-10010

-- Clean up duplicate rows in sequence_feature_chromosome_location_generated
-- for sources whose loaders were inserting without dedup guards:
-- - NCBILoader/ZFIN: markerAssemblyUpdate.sql lacked NOT EXISTS checks
-- - NCBIStartEndLoader: NCBIStartEnd.sql add path lacked NOT EXISTS check
-- Keep the row with the lowest pk_id for each group and delete the rest.

delete from sequence_feature_chromosome_location_generated
where sfclg_location_source in ('NCBILoader', 'ZFIN', 'NCBIStartEndLoader')
  and sfclg_pk_id not in (
    select min(sfclg_pk_id)
    from sequence_feature_chromosome_location_generated
    where sfclg_location_source in ('NCBILoader', 'ZFIN', 'NCBIStartEndLoader')
    group by sfclg_chromosome, sfclg_data_zdb_id, sfclg_acc_num, sfclg_start, sfclg_end,
             sfclg_location_source, sfclg_location_subsource, sfclg_fdb_db_id,
             sfclg_pub_zdb_id, sfclg_assembly, sfclg_gbrowse_track, sfclg_evidence_code,
             sfclg_strand, sfclg_date_created
);
