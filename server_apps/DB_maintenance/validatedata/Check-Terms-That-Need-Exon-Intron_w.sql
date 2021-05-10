select feature_zdb_id, feature_name
 from feature
 where exists (Select 'x' from feature_dna_mutation_detail
       	      	      where fdmd_feature_zdb_id = feature_zdb_id
		      and fdmd_gene_localization_term_zdb_id in (Select term_zdb_id from term where term_name in ('five_prime_cis_splice_site','three_prime_cis_splice_site','splice_junction'))
	         and fdmd_exon_number is null and fdmd_intron_number is null);
