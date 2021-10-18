drop trigger if exists add_color_info_trigger on fluorescent_protein;

create or replace function add_color_info()
returns trigger as
$BODY$
declare add_color_info genotype.geno_display_name%TYPE := scrub_char(NEW.geno_display_name);
declare geno_name_order genotype.geno_name_order%TYPE := zero_pad(NEW.geno_name_order);

begin

    NEW.add_color_info = geno_display_name;

    NEW.geno_name_order = geno_name_order;

    perform p_update_related_fish_names(NEW.geno_zdb_id);
    
    RETURN NEW;
end;
$BODY$ LANGUAGE plpgsql;

create trigger add_color_info_trigger after update on genotype
 for each row
 when (OLD.add_color_info IS DISTINCT FROM NEW.add_color_info)
 execute procedure add_color_info();
