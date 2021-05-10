-- 1 -------------------------------------------------------------------------------------------------------------------

unload to sequence_feature_chromosome_location_generated_temp_records
  select count(*) as counter
  from sequence_feature_chromosome_location_generated_temp;

TEST (sequence_feature_chromosome_location_generated_temp_records < 60000) 'sequence_feature_chromosome_location_generated_temp table has fewer than 60,000 records: $x';
