DROP TRIGGER IF EXISTS feature_community_contribution_audit_trigger ON feature_community_contribution;

DROP TRIGGER IF EXISTS feature_community_contribution_audit_insert_trigger
ON feature_community_contribution;

DROP TRIGGER IF EXISTS feature_community_contribution_audit_update_trigger
ON feature_community_contribution;

CREATE OR REPLACE FUNCTION feature_community_contribution_audit_insert()
  RETURNS trigger AS $BODY$
BEGIN
      PERFORM fcc_event(OLD.fcc_feature_zdb_id,
			OLD.fcc_functional_consequence,
			OLD.fcc_adult_viable,
			OLD.fcc_maternal_zygosity_examined,
			OLD.fcc_currently_available,
			OLD.fcc_other_line_information,
			OLD.fcc_date_added,
			OLD.fcc_added_by);

  RETURN NULL;
END;
$BODY$ LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION feature_community_contribution_audit_update()
  RETURNS trigger AS $BODY$
BEGIN
      PERFORM fcc_event(OLD.fcc_feature_zdb_id,
                        OLD.fcc_functional_consequence,
                        OLD.fcc_adult_viable,
                        OLD.fcc_maternal_zygosity_examined,
                        OLD.fcc_currently_available,
                        OLD.fcc_other_line_information,
                        OLD.fcc_date_added,
                        OLD.fcc_added_by);

  RETURN NULL;
END;
$BODY$ LANGUAGE plpgsql;


CREATE TRIGGER feature_community_contribution_audit_insert_trigger
AFTER INSERT on feature_community_contribution
FOR EACH ROW EXECUTE PROCEDURE feature_community_contribution_audit_insert();


CREATE TRIGGER feature_community_contribution_audit_update_trigger
BEFORE UPDATE on feature_community_contribution
FOR EACH ROW EXECUTE PROCEDURE feature_community_contribution_audit_update();
