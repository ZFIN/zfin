select feature_zdb_id, featurE_name
 from feature
 where exists (SElect 'x' from feature_transcript_mutation_Detail
       	      	      where ftmd_featurE_zdb_id = featurE_zdb_id
		      and (ftmd_exon_number is not null 
		             or ftmd_intron_number is not null
			     )
		      and ftmd_transcript_consequence_term_zdb_id not in (select term_zdb_id from term where term_name in ('intron_gain_variant','exon_loss_variant')));
