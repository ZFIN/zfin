select feature_zdb_id, feature_name
  from feature
  where feature_type not in ('UNSPECIFIED')
 and feature_name not like '%unrecovered'
 and not exists (select 'x' from int_data_source
     	 		where ids_data_zdb_id = feature_zdb_id);