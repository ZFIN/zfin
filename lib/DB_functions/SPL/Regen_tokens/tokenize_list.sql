create procedure tokenize_list()

  -- ---------------------------------------------------------------------
  -- Generates tokens for all the names in the tokenize_in_temp table.
  --
  -- PRECONDITONS:
  --   tokenize_in_temp exists and contains a list of names to tokenize.
  --   tokenize_out_temp exists but does not contain any rows with ZDB IDs
  --     that occur in tokenize_in_temp.
  --
  -- INPUT VARS:
  --   none
  --
  -- OUTPUT VARS:
  --   none
  --
  -- RETURNS:
  --   Success: nothing
  --   Failure: throws whatever exception happened.
  -- 
  -- EFFECTS:
  --   Success:
  --     tokenize_out_temp contains all the tokens for all the names in
  --       in the regen_in_temp table.  IT MAY CONTAINT DUPLICATES.
  --   Error:
  --     tokenize_out_temp may or may not have had data added to it.
  --     transaction is not committed or rolled back.
  -- ---------------------------------------------------------------------

  -- tokenize the names in tokenize_in_temp,  placing the tokens in 
  -- tokenize_out_temp.  Blissfully add duplicates.  They are dropped
  -- later by the calling routine.

  define nameZdbId like zdb_active_data.zactvd_zdb_id;
  define name varchar(255); 
  define nameLength integer;
  define startColumn integer;

  foreach
    select tokin_zdb_id, tokin_name, length(tokin_name)
      into nameZdbId, name, nameLength
      from tokenize_in_temp

    -- Shred the name into every possible substring that starts with a token.
    -- For the string 
    --   "a,b-hydro's gizmo123,version 7"
    -- this code generates substrings in this order:
    --   "a,b-hydro's gizmo123,version 7"
    --     "b-hydro's gizmo123,version 7"
    --       "hydro's gizmo123,version 7"
    --               "gizmo123,version 7"
    --                        "version 7"
    --                                "7"
    let startColumn = 1;
    while startColumn < nameLength
      insert into tokenize_out_temp
          ( tokout_zdb_id, tokout_token )
        values 
          ( nameZdbId, substr(name, startColumn) );
      -- look for end of this token
      let startColumn = startColumn + 1;
      while startColumn < nameLength
        and (   (substr(name, startColumn, 1) between "a" and "z")
             or (substr(name, startColumn, 1) between "0" and "9")
             or (substr(name, startColumn, 1) = "'"))
        let startColumn = startColumn + 1;
      end while
      -- look for start of next token
      let startColumn = startColumn + 1;
      while startColumn < nameLength
        and not (   substr(name, startColumn, 1) between "a" and "z"
                 or substr(name, startColumn, 1) between "0" and "9")
        let startColumn = startColumn + 1;
      end while
    end while -- still have name to process.

  end foreach  -- foreach record in tokenize_in_temp

end procedure;
