drop trigger if exists marker_go_term_evidence_trigger on marker_go_term_evidence;

create or replace function marker_go_term_evidence()
returns trigger as
$BODY$
declare mrkrgoev_notes marker_go_term_evidence.mrkrgoev_notes%TYPE := scrub_char(NEW.mrkrgoev_notes);
begin

     select p_goterm_not_obsolete (
			NEW.mrkrgoev_term_zdb_id);
     select restrictGAFEntries (NEW.mrkrgoev_term_zdb_id, NEW.mrkrgoev_evidence_code);
     select p_marker_has_goterm (
			NEW.mrkrgoev_mrkr_zdb_id,
   			NEW.mrkrgoev_term_zdb_id);
     select p_check_drop_go_root_term (
			NEW.mrkrgoev_mrkr_zdb_id,
			NEW.mrkrgoev_term_zdb_id);
     select p_insert_into_record_attribution_tablezdbids (
			NEW.mrkrgoev_zdb_id,
			NEW.mrkrgoev_source_zdb_id);
     select p_insert_into_record_attribution_datazdbids (
			NEW.mrkrgoev_mrkr_zdb_id,
			NEW.mrkrgoev_source_zdb_id);

     NEW.mrkrgoev_notes = mrkrgoev_notes;
     
     RETURN NEW;

end;
$BODY$ LANGUAGE plpgsql;

create trigger marker_go_term_evidence_trigger before insert or update on marker_go_term_evidence
 for each row
 execute procedure marker_go_term_evidence();
