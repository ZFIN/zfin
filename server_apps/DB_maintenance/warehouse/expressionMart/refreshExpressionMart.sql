--drop FKs.

delete from xpat_exp_details_generated_bkup;
delete from xpat_results_generated_bkup;

insert into xpat_exp_details_generated_bkup
select * from xpat_exp_details_generated;

insert into xpat_results_generated_bkup
select * from xpat_results_generated;

delete from xpat_exp_details_generated;
insert into xpat_exp_details_generated
 select * from xpat_exp_details_generated_temp;

delete from xpat_results_generated;
insert into xpat_results_generated
 select * from xpat_results_generated_temp;

update zdb_flag
  set (zflag_is_on,zflag_last_modified) = ("f",current year to second)
 where zflag_name = "regen_expressionmart" ;

update warehouse_run_tracking
 set wrt_last_loaded_date = current year to second
 where wrt_mart_name = "expression mart";