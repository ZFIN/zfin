DROP TRIGGER IF EXISTS marker_go_term_evidence_before_trigger
ON marker_go_term_evidence;

CREATE OR REPLACE FUNCTION marker_go_term_evidence_before()
  RETURNS trigger AS $BODY$
BEGIN
  NEW.mrkrgoev_notes = scrub_char(NEW.mrkrgoev_notes);
   RETURN NEW;
  raise notice 'ZDB %', NEW.mrkrgoev_term_zdb_id;

END;
$BODY$ LANGUAGE plpgsql;

CREATE TRIGGER marker_go_term_evidence_before_trigger
BEFORE INSERT OR UPDATE ON marker_go_term_evidence
FOR EACH ROW EXECUTE PROCEDURE marker_go_term_evidence_before();




DROP TRIGGER IF EXISTS marker_go_term_evidence_after_trigger
ON marker_go_term_evidence;

CREATE OR REPLACE FUNCTION marker_go_term_evidence_after()
  RETURNS trigger AS $BODY$
BEGIN
  PERFORM p_goterm_not_obsolete(NEW.mrkrgoev_term_zdb_id);
  PERFORM restrictGAFEntries(NEW.mrkrgoev_term_zdb_id, NEW.mrkrgoev_evidence_code);
  PERFORM p_marker_has_goterm(
      NEW.mrkrgoev_mrkr_zdb_id,
      NEW.mrkrgoev_term_zdb_id);
  PERFORM p_check_drop_go_root_term(
      NEW.mrkrgoev_mrkr_zdb_id,
      NEW.mrkrgoev_term_zdb_id);
  PERFORM p_insert_into_record_attribution_tablezdbids(
      NEW.mrkrgoev_zdb_id,
      NEW.mrkrgoev_source_zdb_id);
  PERFORM p_insert_into_record_attribution_datazdbids(
      NEW.mrkrgoev_mrkr_zdb_id,
      NEW.mrkrgoev_source_zdb_id);

  RETURN NEW;
END;
$BODY$ LANGUAGE plpgsql;

CREATE TRIGGER marker_go_term_evidence_after_trigger
BEFORE INSERT OR UPDATE ON marker_go_term_evidence
FOR EACH ROW EXECUTE PROCEDURE marker_go_term_evidence_after();


