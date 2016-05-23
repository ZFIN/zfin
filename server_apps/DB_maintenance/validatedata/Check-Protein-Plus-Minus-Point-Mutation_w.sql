select feature_zdb_id, feature_name
 from feature
 where feature_type = 'POINT_MUTATION'
 and exists (select 'x' from feature_protein_mutation_detail
       	   	  	      where fpmd_feature_zdb_id = feature_zdb_id
			      and (fpmd_number_amino_acids_removed is not null 
			      	   or 
				   fpmd_number_amino_acids_added is not null));
