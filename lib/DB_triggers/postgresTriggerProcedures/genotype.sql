drop trigger if exists genotype_trigger on genotype;

create or replace function genotype()
returns trigger as
$BODY$

declare geno_display_name genotype.geno_display_name%TYPE := scrub_char(NEW.geno_display_name);
declare geno_handle genotype.geno_handle%TYPE := scrub_char(NEW.geno_handle);
declare geno_name_order genotype.geno_name_order%TYPE := zero_pad(NEW.geno_name_order);
declare geno_complexity_order genotype.geno_complexity_order%TYPE := update_geno_sort_order(NEW.geno_zdb_id);

begin
   
     NEW.geno_display_name = geno_display_name;

     NEW.geno_handle = geno_handle;

     NEW.geno_name_order = geno_name_order;

     NEW.geno_complexity_order = geno_complexity_order;
      
     RETURN NEW;

end;
$BODY$ LANGUAGE plpgsql;

create trigger genotype_trigger after insert on genotype
 for each row
 execute procedure genotype();
