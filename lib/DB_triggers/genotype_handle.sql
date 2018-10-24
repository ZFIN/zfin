drop trigger if exists genotype_handle_trigger on genotype;

create or replace function genotype_handle()
returns trigger as
$BODY$
declare geno_handle genotype.geno_handle%TYPE := scrub_char(NEW.geno_handle);
declare geno_complexity_order genotype.geno_complexity_order%TYPE := update_geno_sort_order(NEW.geno_zdb_id);

begin

    NEW.geno_handle = geno_handle;

    NEW.geno_complexity_order = geno_complexity_order;

    perform p_update_related_fish_names(NEW.geno_zdb_id);
    perform p_update_geno_nickname(NEW.geno_zdb_id,
				  NEW.geno_handle);
    RETURN NEW;
end;
$BODY$ LANGUAGE plpgsql;

create trigger genotype_handle_trigger after update on genotype
 for each row
 when (OLD.geno_handle IS DISTINCT FROM NEW.geno_handle)
 execute procedure genotype_handle();
