select feature_zdb_id, feature_name,fdmd_dna_mutation_term_zdb_id,term_name
  from feature 
 where feature_type != 'POINT_MUTATION'
 and exists (Select 'x' from feature_dna_mutation_detail
     	    	    	where fdmd_feature_zdb_id = feature_zdb_id
			and fdmd_dna_mutation_term_zdb_id is not null) and feature_zdb_i=fdmd_feature_zdb_id
			and fdmd_dna_mutation_term_zdb_id=term_zdb_id

