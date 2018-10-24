create or replace function mhist_event( active_marker text, 
	     old_name varchar(255),
	     new_name varchar(255),
	     old_abbrev varchar(150),
	     new_abbrev varchar(150))
returns void as $$
begin
-- This procedure is triggered by updates to Marker.mrkr_name and 
-- Marker.mrkr_abbrev. The procedure is only concerned about GENEs
-- and ignores other marker types. 
-- in Marker_History_Audit.

  insert into marker_history_audit (mha_mrkr_zdb_id, mha_mrkr_abbrev_before, mha_mrkr_abbrev_after,
  	      			   		     mha_mrkr_name_before, mha_mrkr_name_after)
   values (active_marker, old_abbrev, new_abbrev, old_name, new_name);
end
$$ LANGUAGE plpgsql
