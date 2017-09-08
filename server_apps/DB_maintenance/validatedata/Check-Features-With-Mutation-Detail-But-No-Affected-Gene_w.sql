select feature_zdb_id, feature_name
  from feature 
 where not exists (select 'x' 
                     from feature_marker_relationship 
                    where fmrel_ftr_zdb_id = feature_zdb_id 
                      and fmrel_type = 'is allele of') 
   and (exists (select 'x' 
                  from feature_dna_mutation_detail 
                 where fdmd_feature_zdb_id = feature_zdb_id) 
        or exists (select 'x' 
                     from feature_transcript_mutation_detail 
                    where ftmd_feature_zdb_id = feature_zdb_id) 
        or exists (select 'x' 
                     from feature_protein_mutation_detail 
                    where fpmd_feature_zdb_id = feature_zdb_id));

