create function grab_zdb_flag(zdbFlag like zdb_flag.zflag_name)
  returning integer

  -- ---------------------------------------------------------------------
  -- "Grabs" the given zdb_flag record.  Once a record is grabbed, it can't
  -- be grabbed by anyone until it is released.  See release_zdb_flag().
  --
  -- INPUT VARS:
  --   zdbFlag  Name of the flag to grab.  This name must already exist
  --            in the zdb_flag table.
  --
  -- OUTPUT VARS:
  --   none
  --
  -- RETURNS:
  --   0 - Success.  Flag was grabbed
  --   1 - Failed because the flag was already grabbed, or because it does
  --       not exist.
  --
  -- EFFECTS:
  --   Success (0):
  --     zdb_flag.zflag_is_on is set to true for the record, and it was set
  --       to true by this call.
  --     zdb_flag.zflag_last_modified is set to the current time.
  --   Failure (1):
  --     zdb_flag.zflag_is_on is set to true for the record, however it was
  --       already set that way.
  --   Error:
  --     exception is thrown to caller.

  define nRows integer;

  update zdb_flag 
    set zflag_is_on = 't'
    where zflag_name = zdbFlag 
      and zflag_is_on = 'f';

  let nRows = DBINFO('sqlca.sqlerrd2');

  if (nrows == 0) then
	return 1;
  end if
 			
  update zdb_flag set zflag_last_modified = CURRENT
    where zflag_name = zdbFlag;

  return 0;

end function;
