select feature_zdb_id, feature_name, extnote_note
 from feature, external_note
 where not exists (Select 'x' from feature_marker_relationship
       	   	  	  where fmrel_ftr_zdb_id = feature_zdb_id
			  and fmrel_type = 'is allele of')
 and feature_type = 'POINT_MUTATION' and feature_zdb_id=extnote_data_zdb_id;
