------------------------------------------------
--procedure that calls the fimg_overlaps_stg_window function.
--If none of the stage windows defined for the fish image overlap with the
--given stage window, then this procedure will 
--raise and exception which is passed to its parent trigger and will
--not allow the insert or update to procede.
-------------------------------------------------

  create procedure  p_fimg_overlaps_stg_window (vFimgZdbId varchar(50),
						vStartStgZdbId varchar(50),
					    	vEndStgZdbId varchar(50))

  define vOk 	boolean;

  let vOk = fimg_overlaps_stg_window(vFimgZdbId, vStartStgZdbId, vEndStgZdbId);

  if not vOk then
    raise exception -746, 0, "FAIL!: Fish image stage window(s) does not overlap given stage window!!";
  end if;

  end procedure;

