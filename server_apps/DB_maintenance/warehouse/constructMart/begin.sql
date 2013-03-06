begin work ;

set PDQPRIORITY 20;
set lock mode to wait 30; 
set isolation to dirty read;

update zdb_flag
  set (zflag_is_on,zflag_last_modified) = ('t',current year to second)
 where zflag_name = "regen_constructmart";

