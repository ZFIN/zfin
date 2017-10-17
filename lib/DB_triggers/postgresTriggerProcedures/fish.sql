drop trigger if exists fish_trigger on fish;

create or replace function fish()
returns trigger as
$BODY$

declare fish_name fish.fish_name%TYPE := scrub_char(NEW.fish_name);
	fish_name_order fish.fish_name_order%TYPE := zero_pad(fish_name);
	fish_full_name fish.fish_full_name%TYPE := get_fish_full_name(NEW.fish_zdb_id, NEW.fish_genotype_zdb_id, NEW.fish_name);
	fish_functional_affected_gene_count fish.fish_functional_affected_gene_count%TYPE := (select getFishOrder(NEW.fish_zdb_id).numAffectedGene);
	fish_order fish.fish_order%TYPE := (select getFishOrder(NEW.fish_zdb_id).fishOrder);

begin
     
     NEW.fish_name = fish_name;
 
     NEW.fish_name_order = fish_name_order;

     NEW.fish_full_name = fish_full_name;

     NEW.fish_functional_affected_gene_count = fish_functional_affected_gene_count;

     --TODO get_fish_order into fish_order, fish_functional_affected_gene_count

     RETURN NEW;

end;
$BODY$ LANGUAGE plpgsql;

create trigger fish_trigger after update or insert on fish
 for each row
 execute procedure fish();
