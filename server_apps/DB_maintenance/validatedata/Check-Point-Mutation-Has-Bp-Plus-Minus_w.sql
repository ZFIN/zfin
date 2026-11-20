select feature_zdb_id, feature_name,fdmd_dna_mutation_term_zdb_id,fdmd_number_additional_dna_base_pairs,fdmd_number_removed_dna_base_pairs
  from feature, variant, feature_dna_mutation_detail
 where feature_type = 'POINT_MUTATION'
 and exists (Select 'x' from feature_dna_mutation_detail
     	    	    	join variant on var_zdb_id = fdmd_variant_zdb_id
     	    	    	where var_feature_zdb_id = feature_zdb_id
			and (fdmd_dna_mutation_term_zdb_id is null and(
			     fdmd_number_additional_dna_base_pairs is not null
			     or fdmd_number_removed_dna_base_pairs is not null))
			 or (fdmd_dna_mutation_term_zdb_id is not null and(
			     fdmd_number_additional_dna_base_pairs is not null
			     or fdmd_number_removed_dna_base_pairs is not null))
) and feature_zdb_id = var_feature_zdb_id and var_zdb_id = fdmd_variant_zdb_id;
