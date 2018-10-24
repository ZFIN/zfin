DROP TRIGGER IF EXISTS genotype_trigger
ON genotype;

CREATE OR REPLACE FUNCTION genotype()
  RETURNS trigger AS $$
  BEGIN
    NEW.geno_display_name = scrub_char(NEW.geno_display_name);
    NEW.geno_handle = scrub_char(NEW.geno_handle);
    NEW.geno_name_order = zero_pad(NEW.geno_name_order);
    NEW.geno_complexity_order = update_geno_sort_order(NEW.geno_zdb_id);
    RETURN NEW;
  END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER genotype_trigger
BEFORE INSERT ON genotype
FOR EACH ROW
EXECUTE PROCEDURE genotype();
