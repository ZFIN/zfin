select feature_zdb_id, feature_name
  from feature 
 where feature_type != 'POINT_MUTATION'
 and exists (Select 'x' from feature_dna_mutation_detail
     	    	    	where fdmd_feature_zdb_id = feature_zdb_id
			and fdmd_dna_mutation_term_zdb_id is not null);
