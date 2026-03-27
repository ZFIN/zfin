-- 1 -------------------------------------------------------------------------------------------------------------------

unload to sequence_feature_chromosome_location_generated_staging_records
  select count(*) as counter
  from sequence_feature_chromosome_location_generated_staging;

TEST (sequence_feature_chromosome_location_generated_staging_records < 60000) 'sequence_feature_chromosome_location_generated_staging table has fewer than 60,000 records: $x';
