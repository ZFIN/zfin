create procedure regen_genox_create_temp_tables()

  -- ---------------------------------------------------------------------
  -- Creates 2 temp tables used by all the regen_genox functions.
  --
  --
  -- PRECONDITIONS:
  --   regen_genox_input_zdb_id_temp may already exist.
  --   regen_genox_temp may already exist.
  --
  -- INPUT VARS
  --   none.
  --
  -- OUTPUT VARS:
  --   none
  --
  -- RETURNS:
  --   Success: Nothing
  --   Failure: Throws whatever exception happened.
  --
  -- EFFECTS:
  --   Success:
  --     regen_genox_input_zdb_id_temp exists and is empty.
  --     regen_genox_temp exists and is empty.
  --   Error:
  --     None, some, or all of the tables may exist and have data in them.
  --     transaction is not committed or rolled back.
  -- ---------------------------------------------------------------------

  begin
    on exception in (-958, -316)
      -- Ignore these errors:
      --  -958: Temp table already exists.
      --  -316: Index name already exists.
    end exception with resume;


    -- -------------------------------------------------------------------
    --   create regen_genox_input_zdb_id_temp
    -- -------------------------------------------------------------------

    create temp table regen_genox_input_zdb_id_temp  
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

  end

  delete from regen_genox_input_zdb_id_temp;
  delete from regen_genox_temp;

end procedure;
