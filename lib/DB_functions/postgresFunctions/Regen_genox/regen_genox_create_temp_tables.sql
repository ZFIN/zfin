create or replace function regen_genox_create_temp_tables()
  returns void as $$
  -- ---------------------------------------------------------------------
  -- Creates 2 temp tables used by all the regen_genox functions.
  --
  --
  -- PRECONDITIONS:
  --   regen_genox_input_zdb_id_temp may already exist.
  --   regen_genox_temp may already exist.
  -- EFFECTS:
  --   Success:
  --     regen_genox_input_zdb_id_temp exists and is empty.
  --     regen_genox_temp exists and is empty.
  --   Error:
  --     None, some, or all of the tables may exist and have data in them.
  --     transaction is not committed or rolled back.
  -- --------------------------------------------------------------------
  declare errorHint text;
  begin

    -- Contains all the gene zdbIDs/MO zdbIDs and their phenotype-data-related 
    --   genox_zdb_id
   
    drop table if exists mutant_fast_search_new;

    create table mutant_fast_search_new 
      (
        mfs_mrkr_zdb_id text not null,
        mfs_genox_zdb_id text not null
      );
 

    create temporary table if not exists regen_genox_input_zdb_id_temp  
    -- can be either a marker or a genox zdb_id
      (
	rggz_zdb_id		text,
        primary key (rggz_zdb_id)
      );

    delete from regen_genox_input_zdb_id_temp ;

    drop table if exists regen_genox_temp ;

    errorHint = 'create temp table regen_genox_temp';

    create temp table regen_genox_temp
      (
	rggt_mrkr_zdb_id         text not null,
	rggt_genox_zdb_id        text not null
      ) ;

  

  delete from regen_genox_input_zdb_id_temp;
  delete from regen_genox_temp;
 
  exception when raise_exception then
           
            raise notice 'regen_genox_create_temp_tables failed';
  	 

  end;

$$ language plpgsql;
