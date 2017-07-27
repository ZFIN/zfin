create or replace function regen_genofig_process()
returns text as $regen_genofig_process$

  -- ---------------------------------------------------------------------------
  -- Generates Geno / Fig / Term / MO for records in genotype_figure_fast_search table 
-- Any fish which has STR(s)
 begin 
   insert into regen_genofig_temp (rgf_geno_zdb_id,
				rgf_fig_zdb_id,
				rgf_morph_zdb_id,
				rgf_pg_id,
				rgf_fish_Zdb_id,
				rgf_psg_id,
				rgf_genox_zdb_id)
    select distinct fish_genotype_zdb_id,
  	 	  pg_fig_zdb_id,
		  fishstr_str_zdb_id,
		  pg_id,
		  fish_zdb_id,
		  psg_id,
		  genox_Zdb_id
      from fish_experiment, fish, fish_str,
         phenotype_source_generated,
         phenotype_observation_generated,
	 regen_genofig_input_zdb_id_temp
      where fish_zdb_id = genox_fish_zdb_id
       and pg_id = psg_pg_id
       and pg_id = rgfg_id
       and fishstr_fish_zdb_id = fish_Zdb_id
       and pg_genox_zdb_id = genox_zdb_id
       and exists (Select 'x' from mutant_fast_search
     	 		where mfs_genox_zdb_id = genox_zdb_id
			and mfs_mrkr_Zdb_id = fishstr_str_zdb_id);

    insert into regen_genofig_temp (rgf_geno_zdb_id,
				rgf_fig_zdb_id,
				rgf_pg_id,
				rgf_fish_Zdb_id,
				rgf_psg_id,
				rgf_genox_zdb_id)
    select distinct fish_genotype_zdb_id,
  	 	  pg_fig_zdb_id,
		  pg_id,
		  fish_zdb_id,
		  psg_id,
		  genox_Zdb_id
      from fish_experiment, fish,
           phenotype_observation_generated,
           phenotype_source_generated,
	   regen_genofig_input_zdb_id_temp 
      where fish_zdb_id = genox_fish_zdb_id
      and pg_id = psg_pg_id
      and pg_id = rgfg_id
      and pg_genox_zdb_id = genox_zdb_id
      and not exists (Select 'x' from fish_Str where fishstr_fish_zdb_id = fish_Zdb_id)
      and exists (Select 'x' from mutant_fast_search
     	 		where mfs_genox_zdb_id = genox_zdb_id)
      and not exists (Select 'x' from regen_genofig_temp b
       	   	  	  where psg_id = b.rgf_psg_id);

 return 'regen_genox() completed without error; success!';
 exception when raise_exception then
  	    return errorHint; 

 end ;
$regen_genofig_process$ LANGUAGE plpgsql;
