DROP TRIGGER IF EXISTS figure_before_trigger
ON figure;

DROP TRIGGER IF EXISTS figure_after_trigger
ON figure;

CREATE OR REPLACE FUNCTION figure_before()
  RETURNS trigger AS
$BODY$
BEGIN
  NEW.fig_caption = scrub_char(NEW.fig_caption);
  NEW.fig_label = scrub_char(NEW.fig_label);
  NEW.fig_full_label = zero_pad(NEW.fig_label);
  RETURN NEW;
END;
$BODY$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION figure_after()
  RETURNS trigger AS
$BODY$
BEGIN
  PERFORM p_insert_into_record_attribution_tablezdbids(
      NEW.fig_zdb_id,
      NEW.fig_source_zdb_id);
  RETURN NULL;
END;
$BODY$ LANGUAGE plpgsql;


CREATE TRIGGER figure_before_trigger
BEFORE INSERT OR UPDATE ON figure
FOR EACH ROW
EXECUTE PROCEDURE figure_before();

CREATE TRIGGER figure_after_trigger
AFTER INSERT OR UPDATE ON figure
FOR EACH ROW
EXECUTE PROCEDURE figure_after();
