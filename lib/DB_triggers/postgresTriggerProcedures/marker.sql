DROP TRIGGER IF EXISTS marker_name_order
ON marker;

CREATE OR REPLACE FUNCTION marker_name_order()
  RETURNS trigger AS $BODY$
BEGIN
  NEW.mrkr_name = scrub_char(NEW.mrkr_name);
  NEW.mrkr_abbrev = scrub_char(updateAbbrevEqualName(NEW.mrkr_zdb_id,
                                                     NEW.mrkr_name,
                                                     NEW.mrkr_type,
                                                     NEW.mrkr_abbrev));
  NEW.mrkr_abbrev_order = zero_pad(NEW.mrkr_abbrev);
  NEW.mrkr_name_order = scrub_char(zero_pad(NEW.mrkr_name));
  NEW.mrkr_comments = scrub_char(NEW.mrkr_comments);
  RETURN NEW;
END;
$BODY$ LANGUAGE plpgsql;

CREATE TRIGGER marker_name_order_trigger
BEFORE INSERT ON marker
FOR EACH ROW EXECUTE PROCEDURE marker_name_order();


DROP TRIGGER IF EXISTS marker_trigger
ON marker;

CREATE OR REPLACE FUNCTION marker()
  RETURNS trigger AS $BODY$
BEGIN
  PERFORM p_check_mrkr_abbrev(NEW.mrkr_name,
                              NEW.mrkr_abbrev,
                              NEW.mrkr_type);

  PERFORM mhist_event(NEW.mrkr_zdb_id, '', NEW.mrkr_abbrev, '', NEW.mrkr_name);

  PERFORM p_populate_go_root_terms(NEW.mrkr_zdb_id,
                                   NEW.mrkr_name,
                                   NEW.mrkr_type);

  RETURN NULL;
END;
$BODY$ LANGUAGE plpgsql;

CREATE TRIGGER marker_trigger
BEFORE INSERT ON marker
FOR EACH ROW EXECUTE PROCEDURE marker();
