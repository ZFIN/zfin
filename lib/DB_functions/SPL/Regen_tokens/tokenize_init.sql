create procedure tokenize_init()

  -- ---------------------------------------------------------------------
  -- Initiallizes the tokenizer used by the regen tokens routines.
  -- This creates/empties temp tables used by the other tokenize 
  -- routines.
  --
  -- PRECONDITIONS:
  --   tokenize_in_temp may already exist.
  --   tokenize_out_temp may already exist.
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
  --     tokenize_in_temp exists and is empty.
  --     tokenize_out_temp exists and is empty.
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
    --   create tokenize_in_temp
    -- -------------------------------------------------------------------
    create temp table tokenize_in_temp  
      (
	tokin_zdb_id            varchar(50),
        tokin_name              varchar(255)
      ) with NO LOG;


    -- -------------------------------------------------------------------
    --   create tokenize_out_temp
    -- -------------------------------------------------------------------
    create temp table tokenize_out_temp  
      (
	tokout_zdb_id           varchar(50),
        tokout_token            varchar(255)
      ) with NO LOG;

  end

  -- Paranoid code to delete records from the newly created tables.  Why?
  -- Just because, that's why.

  delete from tokenize_in_temp;
  delete from tokenize_out_temp;

end procedure;
