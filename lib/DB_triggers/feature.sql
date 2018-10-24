DROP TRIGGER IF EXISTS feature_before_trigger
ON feature;

DROP TRIGGER IF EXISTS feature_after_trigger
ON feature;

CREATE OR REPLACE FUNCTION feature_before()
  RETURNS trigger AS
$BODY$
BEGIN
  NEW.feature_name = scrub_char(NEW.feature_name);
  NEW.feature_abbrev = scrub_char(NEW.feature_abbrev);
  NEW.feature_name_order = zero_pad(NEW.feature_name_order);
  NEW.feature_abbrev_order = zero_pad(NEW.feature_abbrev_order);
  NEW.feature_line_number = scrub_char(NEW.feature_line_number);
  PERFORM checkFeatureAbbrev(NEW.feature_zdb_id,
                             NEW.feature_type,
                             NEW.feature_abbrev,
                             NEW.feature_lab_prefix_id,
                             NEW.feature_line_number,
                             NEW.feature_df_transloc_complex_prefix,
                             NEW.feature_dominant,
                             NEW.feature_unspecified,
                             NEW.feature_unrecovered,
                             NEW.feature_tg_suffix,
                             NEW.feature_known_insertion_site);
  PERFORM checkDupFeaturePrefixLineDesignation(NEW.feature_lab_prefix_id, NEW.feature_line_number);
  RETURN NEW;
END;
$BODY$ LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION feature_after()
  RETURNS trigger AS
$BODY$
BEGIN
  PERFORM fhist_event(NEW.feature_zdb_id, 'assigned', NEW.feature_name, NEW.feature_abbrev);
  PERFORM populate_feature_Tracking(NEW.feature_abbrev, NEW.feature_name, NEW.feature_zdb_id);
  RETURN NULL;
END;
$BODY$ LANGUAGE plpgsql;

CREATE TRIGGER feature_before_trigger
AFTER INSERT ON feature
FOR EACH ROW EXECUTE PROCEDURE feature_before();

CREATE TRIGGER feature_after_trigger
AFTER INSERT ON feature
FOR EACH ROW EXECUTE PROCEDURE feature_after();
