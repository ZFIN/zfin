begin work;

    create table mutant_fast_search_new 
      (
        mfs_mrkr_zdb_id varchar(50) not null,
        mfs_genox_zdb_id varchar(50) not null
      )
    fragment by round robin in tbldbs1, tbldbs2, tbldbs3
    extent size 512 next size 512 ;
    


    create temp table regen_genox_input_zdb_id_temp  
    -- can be either a marker or a genox zdb_id
      (
	rggz_zdb_id		varchar(50),
        primary key (rggz_zdb_id)
      ) with NO LOG;


    -- -------------------------------------------------------------------
    --   create regen_genox_temp
    -- -------------------------------------------------------------------    
    create temp table regen_genox_temp
      (
	rggt_mrkr_zdb_id         varchar(50) not null,
	rggt_genox_zdb_id        varchar(50) not null
      ) with no log;

insert into regen_genox_temp (rggt_mrkr_zdb_id, rggt_genox_zdb_id)
  select distinct fmrel_mrkr_zdb_id , genox_zdb_id
    from fish, fish_experiment, feature_marker_relationship, genotype_Feature, regen_genox_input_zdb_id_temp
    where fish_zdb_id = genox_fish_Zdb_id
    and fish_genotype_zdb_id = genofeat_geno_zdb_id
    and genofeat_feature_Zdb_id = fmrel_ftr_zdb_id
    and fish_functional_affected_gene_count = 1
    and get_obj_type(fmrel_mrkr_Zdb_id) in ('GENE','MRPHLNO','TALEN','CRISPR')
    and genox_is_std_or_generic_control = 't'
    and fmrel_mrkr_zdb_id = rggz_zdb_id
and not exists (Select 'x' from regen_genox_temp
      	  	 	 where fmrel_mrkr_zdb_id = rggt_mrkr_Zdb_id
			 and genox_zdb_id = rggt_genox_zdb_id);

insert into regen_genox_temp (rggt_mrkr_zdb_id, rggt_genox_zdb_id)
  select fishstr_str_zdb_id, genox_zdb_id
    from fish, fish_str a, fish_experiment, regen_genox_input_zdb_id_temp
    where fish_Zdb_id =a.fishstr_fish_Zdb_id
    and fish_zdb_id = genox_fish_zdb_id
 and fish_functional_affected_gene_count = 1
    and genox_is_std_or_generic_control = 't'
    and fishstr_str_zdb_id = rggz_zdb_id
and not exists (Select 'x' from regen_genox_temp
      	  	 	 where a.fishstr_str_zdb_id = rggt_mrkr_Zdb_id
			 and genox_zdb_id = rggt_genox_zdb_id)
 and not exists (Select 'x' from fish_str b 
     	 		    where a.fishstr_fish_zdb_id = b.fishstr_fish_zdb_id
			    and a.fishstr_str_zdb_id != b.fishstr_str_zdb_id);

insert into regen_genox_temp (rggt_mrkr_zdb_id, rggt_genox_zdb_id)
select mrel_mrkr_2_zdb_id, genox_zdb_id
    from fish, fish_str, fish_experiment, marker_relationship, regen_genox_input_zdb_id_temp
    where fish_Zdb_id =fishstr_fish_Zdb_id
    and fish_zdb_id = genox_fish_zdb_id
    and fish_functional_affected_gene_count =1
    and fishstr_str_zdb_id = mrel_mrkr_1_zdb_id
    and get_obj_type(mrel_mrkr_2_Zdb_id) in ('GENE')
 and genox_is_std_or_generic_control = 't' 
   and mrel_mrkr_2_zdb_id = rggz_zdb_id
and not exists (Select 'x' from regen_genox_temp
      	  	 	 where mrel_mrkr_2_zdb_id = rggt_mrkr_Zdb_id
			 and genox_zdb_id = rggt_genox_zdb_id);
       

delete from regen_genox_temp
  where exists (Select 'x' from fish_experiment, fish,genotype_feature, feature_marker_relationship
  	       	       where rggt_genox_zdb_id = genox_zdb_id
		       and genox_fish_zdb_id = fish_Zdb_id
		       and fish_genotype_zdb_id = genofeat_geno_zdb_id
		       and fmrel_ftr_zdb_id = genofeat_feature_zdb_id
		       and fmrel_type = 'contains phenotypic sequence feature');

rollback work;