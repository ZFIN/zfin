drop trigger if exists genotype_trigger on genotype;

create or replace function genotype()
returns trigger as
$BODY$

declare geno_display_name genotype.geno_display_name%TYPE;
declare geno_handle genotype.geno_handle%TYPE;
declare geno_name_order genotype.geno_name_order%TYPE;
declare geno_complexity_order genotype.geno_complexity_order%TYPE;

begin
     geno_display_name = (select scrub_char(NEW.geno_display_name));
     NEW.geno_display_name = geno_display_name;

     geno_handle = (Select scrub_char(NEW.geno_handle));
     NEW.geno_handle = geno_handle;

     geno_name_order = (Select zero_pad(NEW.geno_name_order));
     NEW.geno_name_order = geno_name_order;

     geno_complexity_order = (select update_geno_sort_order(NEW.geno_zdb_id));
     NEW.geno_complexity_order = geno_complexity_order;
      
     RETURN NEW;

end;
$BODY$ LANGUAGE plpgsql;

create trigger genotype_trigger before insert on genotype
 for each row
 execute procedure genotype();
