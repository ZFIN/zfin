create function release_zdb_flag(zdbFlag like zdb_flag.zflag_name)
  returning integer

  -- ---------------------------------------------------------------------
  -- "Releases" the given zdb_flag record.  Once a record is grabbed, it can't
  -- be grabbed by anyone until it is released.  See grab_zdb_flag().
  --
  -- INPUT VARS:
  --   zdbFlag  Name of the flag to release.  This name must already exist
  --            in the zdb_flag table, and must have been grabbed by the
  --            caller.
  --
  -- OUTPUT VARS:
  --   none
  --
  -- RETURNS:
  --   0 - Success.  Flag was released
  --   1 - Failed because the flag did not exist
  --
  -- EFFECTS:
  --   Success (0):
  --     zdb_flag.zflag_is_on is set to false for the record
  --     zdb_flag.zflag_last_modified is set to the current time.
  --   Error:
  --     exception is thrown to caller.

  define nRows integer;

  update zdb_flag 
    set zflag_is_on = "f", 
        zflag_last_modified = CURRENT
    where zflag_name = zdbFlag
      and zflag_is_on = "t";

  let nRows = DBINFO('sqlca.sqlerrd2');

  if (nrows = 0) then
    return 1;
  end if

  return 0;

end function;
