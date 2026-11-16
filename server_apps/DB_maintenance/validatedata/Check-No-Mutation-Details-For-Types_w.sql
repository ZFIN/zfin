select feature_zdb_id, feature_name
 from feature
 where feature_type not in ('POINT_MUTATION','INDEL',
 'DELETION','INSERTION','TRANSGENIC_INSERTION')
 and (exists (select 'x' from feature_protein_mutation_detail
     	     	     	 join variant on var_zdb_id = fpmd_variant_zdb_id
     	     	     	 where var_feature_zdb_id = feature_zdb_id)
     or exists (Select 'x' from feature_dna_mutation_Detail
     	       	       join variant on var_zdb_id = fdmd_variant_zdb_id
     	       	       where var_feature_zdb_id = feature_zdb_id)
     or exists (Select 'x' from feature_transcript_mutation_detail
     	       	       	   join variant on var_zdb_id = ftmd_variant_zdb_id
     	       	       	   where var_feature_zdb_id = feature_zdb_id));
