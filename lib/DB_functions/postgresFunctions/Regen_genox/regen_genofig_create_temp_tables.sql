create or replace function regen_genofig_create_temp_tables()
returns text as $regen_genofig_create_temp_tables$
  -- ---------------------------------------------------------------------
  -- Creates 4 temp tables used by all the regen_genofig* functions.
  --
  --
  -- PRECONDITIONS:
  --   regen_genofig_clean_exp_with_morph_temp may already exist.
  --   regen_genofig_not_normal_temp may already exist.
  --   regen_genofig_temp
  --   regen_genofig_input_zdb_id_temp
  -- ---------------------------------------------------------------------
  begin

   drop table if exists genotype_figure_fast_search_new;

   create table if not exists genotype_figure_fast_search_new 
      (gffs_serial_id serial8 not null,
        gffs_geno_zdb_id  text not null,
        gffs_fig_zdb_id text not null,
        gffs_morph_zdb_id text,
        gffs_pg_id int8 not null,
	gffs_date_created timestamp 
			  DEFAULT now NOT NULL,         
        gffs_psg_id int8,
	gffs_fish_zdb_id text not null,
	gffs_genox_zdb_id text,
        primary key (gffs_serial_id)	
      );
    
   drop table if exists regen_genofig_input_zdb_id_temp;

   create temporary table if not exists regen_genofig_input_zdb_id_temp  
      (
	rgfg_id	int8
      );

   create index rgfg_id_index 
    on regen_genofig_input_zdb_id_temp (rgfg_id) ;

   drop table if exists regen_genofig_temp;

    -- -------------------------------------------------------------------
    --   create regen_genofig_temp
    -- -------------------------------------------------------------------    
    create  temporary table if not exists regen_genofig_temp
      (rgf_geno_zdb_id		varchar(50) not null,
	rgf_fig_zdb_id		varchar(50) not null,
	rgf_morph_zdb_id	varchar(50),
	rgf_pg_id	int8,
	rgf_fish_zdb_id		varchar(50) not null,
	rgf_psg_id 		int8,
	rgf_genox_zdb_id	varchar(50)
      ) ;

    delete from regen_genofig_temp;
    delete from regen_genofig_input_zdb_id_temp;

  return 'regen_genox() completed without error; success!';
  exception when raise_exception then
  	    return errorHint; 

  end;
$regen_genofig_create_temp_tables$ LANGUAGE plpgsql;
