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

  -- -------------------------------------------------------------------
    -- -------------------------------------------------------------------
    --   Create genotype_figure_fast_search_new
    -- -------------------------------------------------------------------
    -- -------------------------------------------------------------------


    if (exists (select * from systables where tabname = "genotype_figure_fast_search_new")) then
      drop table genotype_figure_fast_search_new;
    end if

    create table genotype_figure_fast_search_new 
      (  
      	gffs_serial_id serial8 not null,
        gffs_geno_zdb_id  varchar(50) not null,
        gffs_fig_zdb_id varchar(50) not null,
        gffs_morph_zdb_id varchar(50),
        gffs_pg_id int8 not null,
	gffs_date_created DATETIME YEAR TO SECOND 
			  DEFAULT CURRENT YEAR TO SECOND NOT NULL,         
        gffs_psg_id int8,
	gffs_fish_zdb_id varchar(50) not null,
	gffs_genox_zdb_id varchar(50),
primary key (gffs_serial_id)	

      )
    fragment by round robin in tbldbs1, tbldbs2, tbldbs3
    extent size 512 next size 512 ;
    

    if (exists (select * from systables where tabname = "regen_genofig_input_zdb_id_temp")) then
      drop table regen_genofig_input_zdb_id_temp;
    end if

    create temp table regen_genofig_input_zdb_id_temp  
      (
	rgfg_id		int8,
        primary key (rgfg_id)
      ) with NO LOG
;

  if (exists (select * from systables where tabname = "regen_genofig_temp")) then
      drop table regen_genofig_temp;
    end if

    -- -------------------------------------------------------------------
    --   create regen_genofig_temp
    -- -------------------------------------------------------------------    
    create temp table regen_genofig_temp
      (
	rgf_geno_zdb_id		varchar(50) not null,
	rgf_fig_zdb_id		varchar(50) not null,
	rgf_morph_zdb_id	varchar(50),
	rgf_pg_id	int8,
	rgf_fish_zdb_id		varchar(50) not null,
	rgf_psg_id 		int8,
	rgf_genox_zdb_id	varchar(50)
      ) 
;

delete from regen_genofig_temp;
delete from regen_genofig_input_zdb_id_temp;

  end 

end procedure;
