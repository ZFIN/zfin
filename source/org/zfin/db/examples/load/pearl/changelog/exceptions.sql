begin work;

update feature_protein_mutation_detail
  set fpmd_number_amino_acids_added = 44
 where fpmd_feature_zdb_id = 'ZDB-ALT-980203-418';

update feature_protein_mutation_detail
  set fpmd_number_amino_acids_added = 28
 where fpmd_feature_zdb_id = 'ZDB-ALT-110127-1';

update feature_protein_mutation_detail
  set fpmd_number_amino_acids_added = 39
 where fpmd_feature_zdb_id = 'ZDB-ALT-110330-2';

update feature_dna_mutation_Detail
 set fdmd_intron_number = '6'
where fdmd_feature_zdb_id = 'ZDB-ALT-110330-2';

update feature_dna_mutation_Detail
 set fdmd_gene_localization_term_zdb_id = (select term_zdb_id from term
     			  	  where term_name = 'five_prime_cis_splice_site')
where fdmd_feature_zdb_id = 'ZDB-ALT-110330-2';

update feature_dna_mutation_detail
 set fdmd_dna_position_start = '946'
 where fdmd_featurE_zdb_id = 'ZDB-ALT-041220-6';

insert into record_attribution(recattrib_data_zdb_id, recattrib_source_zdb_id, recattrib_source_type)
 select fdmd_zdb_id, 'ZDB-PUB-160331-1','feature type'
  from feature, feature_dna_mutation_Detail
 where feature_zdb_id = fdmd_feature_zdb_id;

insert into zdb_active_data
 select fpmd_zdb_id
  from feature_protein_mutation_Detail
 where not exists (Select 'x' from zdb_active_data
       	   	  	  where fpmd_zdb_id = zactvd_zdb_id);

insert into record_attribution(recattrib_data_zdb_id, recattrib_source_zdb_id, recattrib_source_type)
 select fpmd_zdb_id, 'ZDB-PUB-160331-1','feature type'
  from feature, feature_protein_mutation_Detail
 where feature_zdb_id = fpmd_feature_zdb_id;

insert into record_attribution (recattrib_data_zdb_id, recattrib_source_zdb_id, recattrib_source_type)
 select ftmd_zdb_id, 'ZDB-PUB-160331-1','feature type'
  from feature, feature_transcript_mutation_Detail
 where feature_zdb_id = ftmd_feature_zdb_id;

select * from record_attribution
 where recattrib_datA_zdb_id = 'ZDB-ALT-100813-4'
 and recattrib_source_type = 'feature type';




--rollback work;
commit work;
