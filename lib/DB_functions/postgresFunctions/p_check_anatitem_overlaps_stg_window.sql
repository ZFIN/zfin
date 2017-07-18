create or replace function p_check_anatitem_overlaps_stg_Window (
	vAnatItemZdbId text,
	vStartStgZdbId text,
	vEndStgZdbId   text))
returns void as $$
begin
  if not anatitem_overlaps_stg_window (vAnatItemZdbId, 
				       vStartStgZdbId, vEndStgZdbId) then
    raise exception 'FAIL!! Stage window for anatomy does not overlap with the stage window';
  end if;

end
$$ LANGUAGE plpgsql
