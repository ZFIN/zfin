------------------------------------------------
--procedure that calls the fimg_overlaps_stg_window function.
--If none of the stage windows defined for the fish image overlap with the
--given stage window, then this procedure will 
--raise and exception which is passed to its parent trigger and will
--not allow the insert or update to procede.
-------------------------------------------------

  create or replace function  p_fimg_overlaps_stg_window (vFimgZdbId varchar(50),
						vStartStgZdbId varchar(50),
					    	vEndStgZdbId varchar(50))
  returns void as $$
  declare vOk 	boolean := fimg_overlaps_stg_window(vFimgZdbId, vStartStgZdbId, vEndStgZdbId);
  begin 
  if not vOk then
    raise exception 'FAIL!: Fish image stage window(s) does not overlap given stage window';
  end if;
  end

$$ LANGUAGE plpgsql

