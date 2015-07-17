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

  insert into regen_genox_input_zdb_id_temp ( rggz_zdb_id )
      select mrkr_zdb_id from marker where mrkr_type in ("GENE","MRPHLNO","TALEN", "CRISPR");

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
    from fish, fish_str, fish_experiment, regen_genox_input_zdb_id_temp
    where fish_Zdb_id = fishstr_fish_Zdb_id
    and fish_zdb_id = genox_fish_zdb_id
    and fish_functional_affected_gene_count =1
    and genox_is_std_or_generic_control = 't'
    and fishstr_str_zdb_id = rggz_zdb_id
and not exists (Select 'x' from regen_genox_temp
      	  	 	 where fishstr_str_zdb_id = rggt_mrkr_Zdb_id
			 and genox_zdb_id = rggt_genox_zdb_id)
;
 select first 10 fishstr_str_zdb_id, genox_zdb_id, fish_name, genox_exp_Zdb_id
    from fish, fish_str, fish_experiment, regen_genox_input_zdb_id_temp
    where fish_Zdb_id = fishstr_fish_Zdb_id
    and fish_zdb_id = genox_fish_zdb_id
    and fish_functional_affected_gene_count =1
   -- and genox_is_std_or_generic_control = 't'
    and fishstr_str_zdb_id = rggz_zdb_id
and not exists (Select 'x' from regen_genox_temp
      	  	 	 where fishstr_str_zdb_id = rggt_mrkr_Zdb_id
			 and genox_zdb_id = rggt_genox_zdb_id)
;


insert into regen_genox_temp (rggt_mrkr_zdb_id, rggt_genox_zdb_id)
select mrel_mrkr_2_zdb_id, genox_zdb_id
    from fish, fish_str, fish_experiment, marker_relationship, regen_genox_input_zdb_id_temp
    where fish_Zdb_id =fishstr_fish_Zdb_id
    and fish_zdb_id = genox_fish_zdb_id
    and fish_functional_affected_gene_count =1
    and fishstr_str_zdb_id = mrel_mrkr_1_zdb_id
    and get_obj_type(mrel_mrkr_2_Zdb_id) in ('GENE','MRPHLNO','TALEN','CRISPR')
 and genox_is_std_or_generic_control = 't' 
   and mrel_mrkr_2_zdb_id = rggz_zdb_id
and not exists (Select 'x' from regen_genox_temp
      	  	 	 where mrel_mrkr_2_zdb_id = rggt_mrkr_Zdb_id
			 and genox_zdb_id = rggt_genox_zdb_id);
       

     insert into mutant_fast_search_new 
        ( mfs_mrkr_zdb_id, mfs_genox_zdb_id )
      select distinct a.mfs_mrkr_zdb_id, a.mfs_genox_zdb_id
        from mutant_fast_search a
	where not exists (Select 'x' from mutant_fast_search_new b
	      	  	 	 where a.mfs_mrkr_zdb_id = b.mfs_mrkr_zdb_id
				 and a.mfs_genox_zdb_id =b.mfs_genox_zdb_id);

    delete from mutant_fast_search_new
      where mfs_mrkr_zdb_id in
          ( select rggz_zdb_id
              from regen_genox_input_zdb_id_temp ); 

    insert into mutant_fast_search_new 
        ( mfs_mrkr_zdb_id, mfs_genox_zdb_id )
      select distinct rggt_mrkr_zdb_id, rggt_genox_zdb_id
        from regen_genox_temp
	where not exists (Select 'x' from mutant_fast_search_new c
	      	  	 	 where rggt_mrkr_zdb_id = c.mfs_mrkr_zdb_id
				 and rggt_genox_zdb_id =c.mfs_genox_zdb_id);

  
    -- -------------------------------------------------------------------
    --   create indexes; constraints that use them are added at the end.
    -- -------------------------------------------------------------------

  --  let errorHint = "mutant_fast_search_new create PK index";
    create unique index mutant_fast_search_primary_key_index_transient
      on mutant_fast_search_new (mfs_mrkr_zdb_id, mfs_genox_zdb_id)
      fillfactor 100
      in idxdbs1;

 --   let errorHint = "mutant_fast_search_new create another index";
    create index mutant_fast_search_mrkr_zdb_id_foreign_key_index_transient
      on mutant_fast_search_new (mfs_mrkr_zdb_id)
      fillfactor 100
      in idxdbs1;

  --  let errorHint = "mutant_fast_search_new create the third index";
    create index mutant_fast_search_genox_zdb_id_foreign_key_index_transient
      on mutant_fast_search_new (mfs_genox_zdb_id)
      fillfactor 100
      in idxdbs1;

    update statistics high for table mutant_fast_search_new;

    drop table mutant_fast_search;

     -- let errorHint = "rename table ";
      rename table mutant_fast_search_new to mutant_fast_search;


rollback work;