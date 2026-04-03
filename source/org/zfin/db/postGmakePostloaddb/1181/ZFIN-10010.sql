--liquibase formatted sql
--changeset rtaylor:ZFIN-10010

-- Clean up duplicate NCBILoader and ZFIN rows in
-- sequence_feature_chromosome_location_generated.
-- The markerAssemblyUpdate.sql post-processing script was inserting rows
-- without checking for existing records from prior runs.
-- Keep the row with the lowest pk_id for each group and delete the rest.

delete from sequence_feature_chromosome_location_generated
where sfclg_location_source in ('NCBILoader', 'ZFIN')
  and sfclg_pk_id not in (
    select min(sfclg_pk_id)
    from sequence_feature_chromosome_location_generated
    where sfclg_location_source in ('NCBILoader', 'ZFIN')
    group by sfclg_data_zdb_id, sfclg_chromosome, sfclg_start, sfclg_end,
             sfclg_assembly, sfclg_location_source
);
