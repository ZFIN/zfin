copy (
  SELECT
    sfclg_chromosome,
    'ZMP',
    'sequence_alteration',
    sfclg_start,
    sfclg_end,
    '.',
    '+',
    '.',
    'ID=' || feature_zdb_id || ';Name=' || feature_abbrev || ';zdb_id=' || feature_zdb_id || ';Alias=' || feature_zdb_id ||
      ',' || feature_abbrev || ',' || feature_name
  FROM sequence_feature_chromosome_location_generated
  JOIN feature ON feature_zdb_id = sfclg_data_zdb_id
  WHERE sfclg_assembly = 'GRCz11'
  ORDER BY 1,4,5,9 ) to 'zfin_zmp.gff3' DELIMITER '	'
