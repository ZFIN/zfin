create procedure p_check_anatitem_overlaps_stg_Window (
	vAnatItemZdbId like term.term_zdb_id,
	vStartStgZdbId like stage.stg_zdb_id,
	vEndStgZdbId   like stage.stg_zdb_id)

  if not anatitem_overlaps_stg_window (vAnatItemZdbId, 
				       vStartStgZdbId, vEndStgZdbId) then
    raise exception -746, 0, 
      'FAIL!! Stage window for anatomy ' || vAnatItemZdbId || 
      ' does not overlap with the stage window (' || vStartStgZdbId ||
      ',' || vEndStgZdbId || ').';
  end if;

end procedure;
