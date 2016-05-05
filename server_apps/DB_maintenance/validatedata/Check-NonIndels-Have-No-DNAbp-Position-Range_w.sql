select feature_zdb_id, feature_name
 from feature
 where feature_type not in ('INDEL','DELETION','INSERTION')
and exists (Select 'x' from feature_dna_mutation_detail
    	   	   where fdmd_feature_zdb_id = feature_zdb_id
		   and fdmd_dna_position_start is not null
		   and fdmd_dna_position_end is not null);
