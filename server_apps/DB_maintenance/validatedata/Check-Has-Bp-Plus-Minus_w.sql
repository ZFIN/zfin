select feature_zdb_id, feature_name,fdmd_dna_mutation_term_zdb_id,term_name
  from feature , feature_dna_mutation_detail
 where feature_type != 'POINT_MUTATION'
 and exists (Select 'x' from feature_dna_mutation_detail
     	    	    	where fdmd_feature_zdb_id = feature_zdb_id
			and fdmd_dna_mutation_term_zdb_id is not null) and feature_zdb_id=fdmd_feature_zdb_id
			and fdmd_dna_mutation_term_zdb_id=term_zdb_id

