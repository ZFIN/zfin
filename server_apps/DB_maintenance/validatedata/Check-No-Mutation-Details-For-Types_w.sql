select feature_zdb_id, feature_name
 from feature
 where feature_type not in ('POINT_MUTATION','INDEL',
 'DELETION','INSERTION','TRANSGENIC_INSERTION')
 and (exists (select 'x' from feature_protein_mutation_detail
     	     	     	 where fpmd_feature_zdb_id = feature_zdb_id)
     or exists (Select 'x' from feature_dna_mutation_Detail
     	       	       where fdmd_feature_zdb_id = feature_zdb_id)
     or exists (Select 'x' from feature_transcript_mutation_detail
     	       	       	   where ftmd_feature_zdb_id = feature_zdb_id));
