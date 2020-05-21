DROP TRIGGER IF EXISTS fish_trigger
ON fish;
DROP TRIGGER IF EXISTS fish_affected_trigger
ON fish;
drop trigger if exists fish_after
on fish;


CREATE OR REPLACE FUNCTION fish_after()
  RETURNS trigger AS $BODY$

begin                                         
		    update fish
                     set fish_name = get_fish_full_name(NEW.fish_zdb_id,NEW.fish_genotype_zdb_id, get_fish_name(NEW.fish_Zdb_id))
                      where fish_zdb_id = NEW.fish_zdb_id;                                                                    
                     return new;                                    
                     end;     

$BODY$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION fish_trigger()
  RETURNS trigger AS $BODY$
BEGIN
  NEW.fish_name_order = zero_pad(NEW.fish_name); 
  NEW.fish_full_name = NEW.fish_name; 
   RETURN NEW;
END;
$BODY$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION fish_affected()
  RETURNS trigger AS $BODY$
BEGIN


  select * FROM getFishOrder(NEW.fish_zdb_id,NEW.fish_genotype_zdb_id)
 INTO NEW.fish_order,NEW.fish_functional_affected_gene_count;
-- raise notice 'end: %', NEW.fish_functional_affected_gene_count;

-- get_genbank_dblink_length_type
     RETURN NEW;
END;
$BODY$ LANGUAGE plpgsql;

CREATE TRIGGER fish_trigger
BEFORE  INSERT OR UPDATE  ON fish
FOR EACH ROW EXECUTE PROCEDURE fish_trigger();

CREATE TRIGGER fish_affected_trigger
BEFORE  INSERT OR UPDATE  ON fish
FOR EACH ROW EXECUTE PROCEDURE fish_affected();

CREATE TRIGGER fish_after
AFTER  INSERT  ON fish
FOR EACH ROW EXECUTE PROCEDURE fish_after();
