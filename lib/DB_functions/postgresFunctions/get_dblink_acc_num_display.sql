create or replace function get_dblink_acc_num_display (dblinkFdbcontId varchar,
                                            dblinkAccNum varchar)
returns varchar as $dblinkAccNumDisplay$

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
  
  declare len integer;
          index integer;
          ch char(1);
          contigId db_link.dblink_acc_num_display%TYPE;
          dblinkAccNumDisplay  db_link.dblink_acc_num_display%TYPE;
          dblinkDbName varchar;
  begin
  select distinct fdb_db_name 
    into dblinkDbName 
      from foreign_db_contains, foreign_db
      where fdbcont_zdb_id = dblinkFdbcontId 
        and fdb_db_pk_id = fdbcont_fdb_db_id;
  

  if (dblinkDbName = 'WashUZ') then
    -- we need to prepend 'wz' to the acc_num
    dblinkAccNumDisplay := 'wz' || dblinkAccNum;

  --replace the miranda acc num with one that looks more like the 
  --way curators want to display miRNA names at ZFIN.
  --ZDB-FDBCONT-090529-1, dre-let-7g
  --execute function
  --get_dblink_acc_num_display('ZDB-FDBCONT-090529-1','dre-let-7g')
 
  else
    -- spew out exactly what came in
    dblinkAccNumDisplay := dblinkAccNum;
  end if;

  return dblinkAccNumDisplay;
 end

$dblinkAccNumDisplay$ LANGUAGE plpgsql;
