--drop FKs.

delete from expression_experiment_bkup;
delete from expression_result_bkup;
delete from expression_pattern_figure_bkup;

insert into expression_experiment_bkup
select * from expression_experiment;

insert into expression_result_bkup
select * from expression_result;

insert into expression_pattern_figure_bkup
 select * from expression_pattern_figure;

delete from expression_experiment;
insert into expression_experiment
 select * from expression_experiment_temp;

delete from expression_result;
insert into expression_result
 select * from expression_result_temp;

update zdb_flag
  set (zflag_is_on,zflag_last_modified) = ("f",current year to second)
 where zflag_name = "regen_expressionmart" ;

update warehouse_run_tracking
 set wrt_last_loaded_date = current year to second
 where wrt_mart_name = "expression mart";