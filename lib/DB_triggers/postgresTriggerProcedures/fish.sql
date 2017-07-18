drop trigger if exists fish_trigger on fish;

create or replace function fish()
returns trigger as
$BODY$

declare fish_name fish.fish_name%TYPE;
declare fish_name_order fish.fish_name_order%TYPE;
declare fish_full_name fish.fish_full_name%TYPE;

begin
     
     fish_name = (select scrub_char(fish_name));
     NEW.fish_name = fish_name;
 
     fish_name_order = (select zero_pad(fish_name_order));
     NEW.fish_name_order = fish_name_order;

     fish_full_name = (select get_fish_full_name(NEW.fish_zdb_id, NEW.fish_genotype_zdb_id, NEW.fish_name);
     NEW.fish_full_name = fish_full_name;

     --TODO get_fish_order into fish_order, fish_functional_affected_gene_count

     RETURN NEW;

end;
$BODY$ LANGUAGE plpgsql;

create trigger fish_trigger before update or insert on fish
 for each row
 execute procedure fish();
