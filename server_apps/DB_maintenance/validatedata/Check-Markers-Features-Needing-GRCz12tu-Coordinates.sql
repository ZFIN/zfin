SELECT sfclg_data_zdb_id AS entity_id,
       get_obj_type(sfclg_data_zdb_id) AS entity_type,
       CASE
           WHEN get_obj_type(sfclg_data_zdb_id) = 'ALT' THEN (SELECT feature_abbrev FROM feature WHERE feature_zdb_id = sfclg_data_zdb_id)
           ELSE (SELECT mrkr_abbrev FROM marker WHERE mrkr_zdb_id = sfclg_data_zdb_id)
       END AS entity_name,
       sfclg_assembly AS current_assembly,
       sfclg_location_source AS location_source,
       sfclg_chromosome AS chromosome,
       sfclg_start AS start_pos,
       sfclg_end AS end_pos
FROM sequence_feature_chromosome_location_generated old
WHERE sfclg_assembly IS DISTINCT FROM 'GRCz12tu'
  AND NOT EXISTS (
    SELECT 1
    FROM sequence_feature_chromosome_location_generated new
    WHERE new.sfclg_data_zdb_id = old.sfclg_data_zdb_id
      AND new.sfclg_assembly = 'GRCz12tu'
  )
ORDER BY sfclg_assembly, get_obj_type(sfclg_data_zdb_id), sfclg_data_zdb_id;
