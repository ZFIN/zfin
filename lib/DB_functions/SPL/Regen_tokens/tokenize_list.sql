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
  define token varchar(255);
  define nameLength integer;
  define startColumn integer;
  define endColumn integer;

  foreach
    select tokin_zdb_id, tokin_name, length(tokin_name)
      into nameZdbId, name, nameLength
      from tokenize_in_temp

    -- scrub stray HTML (we may need a function for this)
    let name = replace(name,"<sup>"," ");
    let name = replace(name,"</sup>"," ");
    let name = replace(name,"<sub>"," ");
    let name = replace(name,"</sub>"," ");
    let name = replace(name,"<i>"," ");
    let name = replace(name,"</i>"," ");
    let name = scrub_char(name);

    -- Split the name into tokens.  A token (word) is defined by the following
    -- characters (regex format):  ([a-z0-9]+[:'][a-z0-9]+)|([a-z0-9]+)
    -- 
    -- VALID token (examples):  gizmo123, wu:123, hydro's, version, 7, a, b
    -- 
    -- Originally, we...
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
    --
    -- But this proved to be overkill, so we simply tokenize each "word."  
    -- The advantage to this is we can use "=" instead of "like" in our
    -- SQL query for matching tokens -- efficiency.

    let startColumn = 1;

    -- look for start of first token
    while startColumn <= nameLength
      and not (   substr(name, startColumn, 1) between "a" and "z"
               or substr(name, startColumn, 1) between "0" and "9")
      let startColumn = startColumn + 1;
    end while

    while startColumn <= nameLength
      -- look for end of this token
      let endColumn = startColumn + 1;
      while endColumn <= nameLength
        and (   (substr(name, endColumn, 1) between "a" and "z")
             or (substr(name, endColumn, 1) between "0" and "9")
             or (substr(name, endColumn, 1) = "'")
             or (substr(name, endColumn, 1) = ":"))
        let endColumn = endColumn + 1;
      end while

      -- insert token
      let token = substr(name, startColumn, endColumn - startColumn);
      let token = scrub_char(token);
      if (token is not null) then
        insert into tokenize_out_temp
          ( tokout_zdb_id, tokout_token )
          values 
            ( nameZdbId, token );
      end if

      -- look for start of next token
      let startColumn = endColumn + 1;
      while startColumn <= nameLength
        and not (   substr(name, startColumn, 1) between "a" and "z"
                 or substr(name, startColumn, 1) between "0" and "9")
        let startColumn = startColumn + 1;
      end while

    end while -- still have name to process.

  end foreach  -- foreach record in tokenize_in_temp

end procedure;
