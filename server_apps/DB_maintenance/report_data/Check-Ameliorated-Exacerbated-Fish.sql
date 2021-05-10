select distinct fish_zdb_id, 
 		fish_name, 
		genox_exp_zdb_id, 
		exp_name, 
		psg_short_name, 
		fig_source_zdb_id 
    from experiment, 
	figure,genotype, 
	fish, 
	fish_experiment, 
	phenotype_source_generated, 
	phenotype_observation_generated  
     where (psg_short_name like '%ameliorated%' or psg_short_name like '%exacerbated%'
	and psg_pg_id = pg_id 
	and genox_Zdb_id = pg_genox_zdb_id 
	and genox_fish_zdb_id = fish_zdb_id 
	and fish_genotype_Zdb_id = geno_Zdb_id 
	and  geno_is_wildtype = 't' 
	and fish_functional_Affected_gene_count = 1 
	and genox_is_std_or_generic_control = 't' 
	and fig_zdb_id = pg_fig_zdb_id 
	and exp_zdb_id = genox_exp_zdb_id;
