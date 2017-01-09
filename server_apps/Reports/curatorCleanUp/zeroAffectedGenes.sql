select distinct fish_zdb_id as id, fish_name as name
 from fish, fish_experiment, phenotype_experiment
 where fish_Zdb_id = genox_fish_zdb_id
 and phenox_genox_zdb_id = genox_zdb_id
 and fish_functional_affected_gene_count = 0
 and genox_is_std_or_generic_control = 't'
into temp tmp_fish;

delete from tmp_fish
 where exists (Select 'x' from fish, 
       	      	      	  genotype_feature, 
			  feature_marker_Relationship
			  where fish_Zdb_id = id
			  and genofeat_geno_zdb_id = fish_genotype_zdb_id
			  and fmrel_ftr_zdb_id = genofeat_feature_zdb_id
			  and fmrel_type = 'contains phenotypic sequence feature');

unload to fishes.txt
select * from tmp_fish;
       	       
