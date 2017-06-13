------------------------------------------------
--procedure that calls the stg_window_consistant function.
--if the end stage entered or updated in one of the tables that enters stages 
--is greater than the beginning stage entered or updated, this procedure will 
--raise and exception which is passed to its parent trigger and will
--not allow the insert or update to procede.
-------------------------------------------------

  create or replace function  p_stg_hours_consistent (vStartZDB varchar(50), 
					    vEndZDB varchar(50))
  returns void as $$
  declare vOk 	boolean := stg_window_consistent(vStartZDB, vEndZDB);
  begin
  if not vOk then
    raise exception 'FAIL!: start hours > end hours';
  end if;
  end
$$ LANGUAGE plpgsql
