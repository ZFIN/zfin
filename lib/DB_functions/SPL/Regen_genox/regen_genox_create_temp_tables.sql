create procedure regen_genox_create_temp_tables()

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




    -- -------------------------------------------------------------------
    --   create regen_genox_input_zdb_id_temp
    -- -------------------------------------------------------------------
  begin
    on exception in (-958, -316)
      -- Ignore these errors:
      --  -958: Temp table already exists.
      --  -316: Index name already exists.
    end exception with resume;

    -- -------------------------------------------------------------------
    -- -------------------------------------------------------------------
    --   CREATE mutant_fast_search_newb (mutant_fast_search) TABLE
    -- -------------------------------------------------------------------
    -- -------------------------------------------------------------------

    -- Contains all the gene zdbIDs/MO zdbIDs and their phenotype-data-related 
    --   genox_zdb_id

    --let errorHint = "mutant_fast_search";

    if (exists (select * from systables where tabname = "mutant_fast_search_new")) then
      drop table mutant_fast_search_new;
    end if

    create table mutant_fast_search_new 
      (
        mfs_mrkr_zdb_id varchar(50) not null,
        mfs_genox_zdb_id varchar(50) not null
      )
    fragment by round robin in tbldbs1, tbldbs2, tbldbs3
    extent size 512 next size 512 ;
    
  if (exists (select * from systables where tabname = "regen_genox_input_zdb_id_temp ")) then
      drop table regen_genox_input_zdb_id_temp ;
    end if


    create temp table regen_genox_input_zdb_id_temp  
    -- can be either a marker or a genox zdb_id
      (
	rggz_zdb_id		varchar(50),
        primary key (rggz_zdb_id)
      ) with NO LOG;

 if (exists (select * from systables where tabname = "regen_genox_temp ")) then
      drop table regen_genox_temp ;
    end if

 if (exists (select * from systables where tabname = "regen_genox_construct_temp ")) then
      drop table regen_genox_construct_temp ;
    end if


    -- -------------------------------------------------------------------
    --   create regen_genox_temp
    -- -------------------------------------------------------------------    
   create temp table regen_genox_construct_temp
      (
	rggt_construct_zdb_id         varchar(50) not null,
	rggt_genox_zdb_id        varchar(50) not null
      ) with no log;

    create temp table regen_genox_temp
      (
	rggt_mrkr_zdb_id         varchar(50) not null,
	rggt_genox_zdb_id        varchar(50) not null
      ) with no log;

  end



  delete from regen_genox_input_zdb_id_temp;
  delete from regen_genox_construct_temp;
  delete from regen_genox_temp;

end procedure;
