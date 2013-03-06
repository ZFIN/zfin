--drop FKs.



delete from chromosome_search_backup;

insert into chromosome_search_backup
select * from chromosome_search;

update zdb_flag
  set (zflag_is_on,zflag_last_modified) = ("t",current year to second)
 where zflag_name = "regen_chromosomemart" ;

delete from chromosome_search;
insert into chromosome_search
 select * from chromosome_search_temp;

update zdb_flag
  set (zflag_is_on,zflag_last_modified) = ("f",current year to second)
 where zflag_name = "regen_chromosomemart" ;

update warehouse_run_tracking
 set wrt_last_loaded_date = current year to second
 where wrt_mart_name = "chromosome mart";