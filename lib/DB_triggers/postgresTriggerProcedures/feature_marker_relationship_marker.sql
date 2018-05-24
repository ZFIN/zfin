drop trigger if exists feature_marker_relationship_marker_trigger on feature_marker_relationship;

create or replace function feature_marker_relationship_marker()
returns trigger as
$BODY$

begin
    	perform p_markers_present_absent_exclusive(NEW.fmrel_mrkr_zdb_id, NEW.fmrel_ftr_zdb_id, NEW.fmrel_type);
        perform p_update_fmrel_genotype_names(NEW.fmrel_mrkr_zdb_id,
		OLD.fmrel_mrkr_zdb_id);
        perform p_fmrel_grpmem_correct(
		NEW.fmrel_ftr_zdb_id,
		NEW.fmrel_mrkr_zdb_id,
		NEW.fmrel_type)	;
     
     RETURN null;

end;
$BODY$ LANGUAGE plpgsql;

create trigger feature_marker_relationship_marker_trigger before update on feature_marker_relationship
  for each row
 WHEN (OLD.fmrel_mrkr_zdb_id IS DISTINCT FROM NEW.fmrel_mrkr_zdb_id)
 execute procedure feature_marker_relationship_marker();
