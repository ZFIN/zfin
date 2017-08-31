create procedure set_session_params()

  -- ---------------------------------------------------------------------
  -- Sets a standard set of Informix engine parameters.  Once set, these
  -- apply for the life of the connection, or until they are overridden.
  --
  -- INPUT VARS:
  --   none
  --
  -- OUTPUT VARS:
  --   none
  --
  -- RETURNS:
  --   Success: Nothing
  --   Error:   Throws whatever exception it encountered.
  --
  -- EFFECTS:
  --   Success:
  --     LOCK MODE, ISOLATION, and PDQPRIORITY are all set to ZFIN standard
  --       values
  --   Failure:
  --     None, some, or all of the above may have been set.

  -- crank up the parallelism.
  set pdqpriority high;

  -- Wait up to 5 seconds on locks
  set lock mode to wait 5;

  -- Set isolation to dirty read.  Can't use ANSI set transaction statement,
  -- because that requires you to be in a transaction at the time you set
  -- the isolation mode.
  set isolation to dirty read;

end procedure;
