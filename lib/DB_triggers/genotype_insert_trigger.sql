create trigger genotype_insert_trigger insert on genotype
  referencing new as new_genotype
    for each row
    (
        execute function scrub_char(new_genotype.geno_display_name ) 
    into genotype.geno_display_name,
        execute function scrub_char(new_genotype.geno_handle ) 
    into genotype.geno_handle,
        execute function zero_pad(new_genotype.geno_display_name ) 
    into genotype.geno_name_order,
    	execute function update_geno_sort_order(new_genotype.geno_zdb_id)
    into genotype.geno_complexity_order

);