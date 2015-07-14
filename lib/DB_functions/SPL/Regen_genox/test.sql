begin work;

    create temp table regen_genofig_input_zdb_id_temp  
      (
	rgfg_id		varchar(50),
        primary key (rgfg_id)
      ) with NO LOG
;
  create table genotype_figure_fast_search_new 
      (  
        gffs_geno_zdb_id  varchar(50) not null,
        gffs_fig_zdb_id varchar(50) not null,
        gffs_superterm_zdb_id varchar(50) not null,
        gffs_subterm_zdb_id varchar(50),
        gffs_quality_zdb_id varchar(50) not null,
        gffs_tag varchar(25) not null,
        gffs_morph_zdb_id varchar(50),
        gffs_phenox_pk_id int8 not null,
	gffs_date_created DATETIME YEAR TO SECOND 
			  DEFAULT CURRENT YEAR TO SECOND NOT NULL,         
        gffs_phenos_id int8 not null,
	gffs_fish_zdb_id varchar(50) not null,
	gffs_genox_zdb_id varchar(50),
    gffs_serial_id serial8 not null 

      )
    fragment by round robin in tbldbs1, tbldbs2, tbldbs3
    extent size 512 next size 512 ;
    


    -- -------------------------------------------------------------------
    --   create regen_genofig_temp
    -- -------------------------------------------------------------------    
    create temp table regen_genofig_temp
      (
	rgf_geno_zdb_id		varchar(50) not null,
	rgf_fig_zdb_id		varchar(50) not null,
	rgf_morph_zdb_id	varchar(50),
	rgf_phenox_pk_id	int8,
	rgf_fish_zdb_id		varchar(50) not null,
	rgf_phenos_id 		int8,
	rgf_genox_zdb_id	varchar(50)
      ) 
;

    insert into regen_genofig_input_zdb_id_temp ( rgfg_id )
      select phenox_pk_id from phenotype_experiment;
insert into regen_genofig_temp (rgf_geno_zdb_id,
				rgf_fig_zdb_id,
				rgf_morph_zdb_id,
				rgf_phenox_pk_id,
				rgf_fish_Zdb_id,
				rgf_phenos_id,
				rgf_genox_zdb_id)
  select distinct fish_genotype_zdb_id,
  	 	  phenox_fig_zdb_id,
		  fishstr_str_zdb_id,
		  phenox_pk_id,
		  fish_zdb_id,
		  phenos_pk_id,
		  genox_Zdb_id
    from fish_experiment, fish, fish_str,
         phenotype_statement,
         phenotype_experiment,
	 regen_genofig_input_zdb_id_temp
   where fish_zdb_id = genox_fish_zdb_id
     and phenox_pk_id = phenos_phenox_pk_id
     and phenox_pk_id = rgfg_id
     and fishstr_fish_zdb_id = fish_Zdb_id
     and phenox_genox_zdb_id = genox_zdb_id
     and exists (Select 'x' from mutant_fast_search
     	 		where mfs_genox_zdb_id = genox_zdb_id);

insert into regen_genofig_temp (rgf_geno_zdb_id,
				rgf_fig_zdb_id,
				rgf_phenox_pk_id,
				rgf_fish_Zdb_id,
				rgf_phenos_id,
				rgf_genox_zdb_id)
  select distinct fish_genotype_zdb_id,
  	 	  phenox_fig_zdb_id,
		  phenox_pk_id,
		  fish_zdb_id,
		  phenos_pk_id,
		  genox_Zdb_id
    from fish_experiment, fish,
         phenotype_statement,
         phenotype_experiment,
	 regen_genofig_input_zdb_id_temp
   where fish_zdb_id = genox_fish_zdb_id
     and phenox_pk_id = phenos_phenox_pk_id
   and phenox_pk_id = rgfg_id
 and phenox_genox_zdb_id = genox_zdb_id
   and not exists (Select 'x' from fish_Str where fishstr_fish_zdb_id = fish_Zdb_id)
     and exists (Select 'x' from mutant_fast_search
     	 		where mfs_genox_zdb_id = genox_zdb_id);


  delete from genotype_figure_fast_search
   where exists (Select 'x' from regen_genofig_input_zdb_id_temp
   	 		where gffs_phenos_id = rgfg_id);

  insert into genotype_figure_fast_search_new
    select * from genotype_Figure_fast_Search;

  insert into genotype_figure_fast_search_new
      (gffs_geno_zdb_id,
	gffs_fig_zdb_id,
	gffs_morph_zdb_id,
	gffs_phenox_pk_id,
	gffs_fish_zdb_id,
	gffs_phenos_id,
	gffs_genox_Zdb_id)
    select distinct rgf_geno_zdb_id,
    	   rgf_fig_zdb_id,
	   rgf_morph_zdb_id,
	   rgf_phenox_pk_id,
	   rgf_fish_zdb_id,
	   rgf_phenos_id,
	   rgf_genox_zdb_id
      from regen_genofig_temp;


select * From genotype_figure_fast_search_new;

rollback work;