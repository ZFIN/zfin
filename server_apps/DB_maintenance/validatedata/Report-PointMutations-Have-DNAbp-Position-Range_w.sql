select feature_zdb_id, feature_name,fdmd_dna_position_start,fdmd_dna_position_end
 from feature, feature_dna_mutation_detail
 where feature_type not in ('INDEL','DELETION','INSERTION')
and exists (Select 'x' from feature_dna_mutation_detail
    	   	   where fdmd_feature_zdb_id = feature_zdb_id
		   and fdmd_dna_position_start is not null
		   and fdmd_dna_position_end is not null and fdmd_dna_position_start != fdmd_dna_position_end) and feature_zdb_id=fdmd_feature_zdb_id;
