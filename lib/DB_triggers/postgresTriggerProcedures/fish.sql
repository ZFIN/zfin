DROP TRIGGER IF EXISTS fish_trigger
ON fish;

CREATE OR REPLACE FUNCTION fish()
  RETURNS trigger AS $BODY$
DECLARE fish_functional_affected_gene_count fish.fish_functional_affected_gene_count%type;
        fish_order                          fish.fish_order%type;

BEGIN
  SELECT *
  FROM getFishOrder(NEW.fish_zdb_id)
  INTO fish_order, fish_functional_affected_gene_count;

  NEW.fish_name = scrub_char(NEW.fish_name);
  NEW.fish_name_order = zero_pad(NEW.fish_name);
  NEW.fish_full_name = get_fish_full_name(NEW.fish_zdb_id, NEW.fish_genotype_zdb_id, NEW.fish_name);
  NEW.fish_order = fish_order;
  NEW.fish_functional_affected_gene_count = fish_functional_affected_gene_count;

  RETURN NEW;
END;
$BODY$ LANGUAGE plpgsql;

CREATE TRIGGER fish_trigger
BEFORE UPDATE OR INSERT ON fish
FOR EACH ROW
EXECUTE PROCEDURE fish();
