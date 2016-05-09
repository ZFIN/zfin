select feature_zdb_id, feature_name
 from feature
 where feature_type = 'TRANSGENIC_INSERTION'
and exists (Select 'x' from feature_dna_mutation_detail
    	   	   where fdmd_feature_zdb_id =feature_zdb_id
		   and (fdmd_dna_position_start is not null
		       or fdmd_dna_position_end is not null
		       or fdmd_dna_sequence_of_reference_accession_number is not null
		       or fdmd_dna_mutation_term_zdb_id is not null
		       or fdmd_number_additional_dna_base_pairs is not null
		       or fdmd_number_removed_dna_base_pairs is not null));
