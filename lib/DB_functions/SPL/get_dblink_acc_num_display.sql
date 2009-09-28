create function get_dblink_acc_num_display (dblinkFdbId varchar(50),
                                            dblinkAccNum varchar(50))
returning varchar(50);

  -- For most db links we display the link as db_name:acc_num
  -- However, for a few databases, the acc_num is not a good choice
  -- for display, either because it has too much information, or not
  -- enough.  This function takes in a db name and accession number
  -- and returns the accession number in a format that is better for
  -- displaying.
  -- For most databases, the accession number is returned in exactly
  -- the same format it came in in.

  -- Both of the input params are part of the PK of db_link.
  -- Therefore they can't be NULL.
  -- However, it is possible that the acc_num is not well formed,
  -- in which case this routine may return a NULL.
  
  define len integer;
  define index integer;
  define ch char(1);
  define contigId like db_link.dblink_acc_num_display;
  define dblinkAccNumDisplay like db_link.dblink_acc_num_display;
  define dblinkDbName varchar(50);
  
  select distinct fdb_db_name 
    into dblinkDbName 
      from foreign_db_contains, foreign_db
      where fdbcont_zdb_id = dblinkFdbId 
        and fdb_db_pk_id = fdbcont_fdb_db_id;
  
  if (dblinkDbName = "WEB_FPC") then
    -- The acc_num will look something like
    --   12595&marker=stID23165.16
    -- or, generally
    --   contig_id&marker=marker_id
    -- In this case we only want to condense this down to:
    --   contig_id:marker_id
    -- Scan until hitting the &

    let len = length(dblinkAccNum);
    let index = 1;
    let ch = substr(dblinkAccNum, index, 1);
    while (index < len and ch <> "&")
      let index = index + 1;
      let ch = substr(dblinkAccNum, index, 1);
    end while;

    let contigId = substr(dblinkAccNum, 1, index - 1);

    -- check for "&marker=" and then pull the remainder of the string

    if (substr(dblinkAccNum, index, 8) = "&marker=") then
      let dblinkAccNumDisplay = 
		substr(dblinkAccNum, index + 8) || "," || contigId;
    else
      -- not well formatted, return null
      let dblinkAccNumDisplay = NULL::varchar(50);
    end if

  elif (dblinkDbName = "WashUZ") then
    -- we need to prepend "wz" to the acc_num
    let dblinkAccNumDisplay = "wz" || dblinkAccNum;

  --replace the miranda acc num with one that looks more like the 
  --way curators want to display miRNA names at ZFIN.
  --ZDB-FDBCONT-090529-1, dre-let-7g
  --execute function
  --get_dblink_acc_num_display('ZDB-FDBCONT-090529-1','dre-let-7g')

  elif (dblinkDbName = "MIRANDA") then
    let dblinkAccNumDisplay = lower(dblinkAccNum);
    if (dblinkAccNum like 'dre-let%') then
       let dblinkAccNumDisplay = replace(dblinkAccNum,'dre-let-','mirlet'); 
    else
       let dblinkAccNumDisplay = replace(dblinkAccNumDisplay,'dre-mir','mir');   
    end if;

  else
    -- spew out exactly what came in
    let dblinkAccNumDisplay = dblinkAccNum;
  end if;

  return dblinkAccNumDisplay;

end function;
