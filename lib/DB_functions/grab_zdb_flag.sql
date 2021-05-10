create or replace function grab_zdb_flag(zdbFlag text)
returns int as $grab_zdb_flag$

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

  declare nRows integer;
  begin

  select count(*) into nRows from zdb_flag where zflag_name = zdbFlag;
  if (nRows != 0) then
	return 1;
  end if;
 			
  update zdb_flag set zflag_last_modified = NOW()
    where zflag_name = zdbFlag;

  return 0;

end;


$grab_zdb_flag$ LANGUAGE plpgsql
