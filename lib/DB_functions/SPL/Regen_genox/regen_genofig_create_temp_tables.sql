create procedure regen_genofig_create_temp_tables()

  -- ---------------------------------------------------------------------
  -- Creates 4 temp tables used by all the regen_genofig* functions.
  --
  --
  -- PRECONDITIONS:
  --   regen_genofig_clean_exp_with_morph_temp may already exist.
  --   regen_genofig_not_normal_temp may already exist.
  --   regen_genofig_temp
  --   regen_genofig_input_zdb_id_temp
  --
  -- INPUT VARS
  --   none.
  --
  -- OUTPUT VARS:
  --   none
  -- ---------------------------------------------------------------------

    -- -------------------------------------------------------------------
    --   create regen_genofig_input_zdb_id_temp
    -- -------------------------------------------------------------------
  begin
    on exception in (-958, -316)
      -- Ignore these errors:
      --  -958: Temp table already exists.
      --  -316: Index name already exists.
    end exception with resume;

    create temp table regen_genofig_input_zdb_id_temp  
      (
	rgfg_zdb_id		varchar(50),
        primary key (rgfg_zdb_id)
      ) with NO LOG;

    -- -------------------------------------------------------------------
    --   create regen_genofig_clean_exp_with_morph_temp
    -- -------------------------------------------------------------------

    create temp table regen_genofig_clean_exp_with_morph_temp  
      (
	rgfcx_clean_exp_zdb_id		varchar(50),
	rgfcx_morph_zdb_id		varchar(50)
      ) with NO LOG;

    create index regen_genofig_clean_exp_with_morph_temp_foreign_key
    on regen_genofig_clean_exp_with_morph_temp (rgfcx_clean_exp_zdb_id) 
    using btree in idxdbs1;



    -- -------------------------------------------------------------------
    --   create regen_genofig_not_normal_temp
    -- -------------------------------------------------------------------

    create temp table regen_genofig_not_normal_temp  
      (        
	rgfnna_id		int8,
	rgfnna_genox_zdb_id	varchar(50),
	rgfnna_phenos_id	int8
      ) with NO LOG;

    create index regen_genofig_not_normal_temp_primary_foreign_key 
    on regen_genofig_not_normal_temp (rgfnna_genox_zdb_id) 
    using btree in idxdbs2;

     


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
      ) with no log;

  end 
  delete from regen_genofig_clean_exp_with_morph_temp;
  delete from regen_genofig_temp;

end procedure;
