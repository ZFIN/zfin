drop trigger if exists genotype_display_name_trigger on genotype;

create or replace function genotype_display_name()
returns trigger as
$BODY$
declare geno_display_name genotype.geno_display_name%TYPE := scrub_char(NEW.geno_display_name);
declare geno_name_order genotype.geno_name_order%TYPE := zero_pad(NEW.geno_name_order);

begin

    NEW.geno_display_name = geno_display_name;

    NEW.geno_name_order = geno_name_order;

    perform p_update_related_fish_names(NEW.geno_zdb_id);
    
    RETURN NEW;
end;
$BODY$ LANGUAGE plpgsql;

create trigger genotype_display_name_trigger after update on genotype
 for each row
 when (OLD.geno_display_name IS DISTINCT FROM NEW.geno_display_name)
 execute procedure genotype_display_name();
