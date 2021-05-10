begin work ;

update zdb_flag
  set (zflag_is_on,zflag_last_modified) = ('t', now())
 where zflag_name = 'regen_chromosomemart';

