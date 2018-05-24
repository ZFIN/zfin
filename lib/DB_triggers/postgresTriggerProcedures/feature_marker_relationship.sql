drop trigger if exists feature_marker_relationship_trigger on feature_marker_relationship;

create or replace function feature_marker_relationship()
returns trigger as
$BODY$

begin
     perform p_mrel_grpmem_correct (
           NEW.fmrel_ftr_zdb_id, 
           NEW.fmrel_mrkr_zdb_id, 
           NEW.fmrel_type
         );
     perform p_markers_present_absent_exclusive(NEW.fmrel_mrkr_zdb_id, 
								NEW.fmrel_ftr_zdb_id, 
								NEW.fmrel_type);
	perform p_update_unspecified_alleles(NEW.fmrel_mrkr_zdb_id,
							NEW.fmrel_ftr_zdb_id);
	perform p_update_related_genotype_names(NEW.fmrel_ftr_zdb_id);
     
     RETURN null;

end;
$BODY$ LANGUAGE plpgsql;

create trigger feature_marker_relationship_trigger before insert on feature_marker_relationship
 for each row
 execute procedure feature_marker_relationship();
