create trigger geno_display_name_update_trigger update of geno_display_name
  on genotype referencing new as new_genotype
    for each row
    (
        execute function scrub_char(new_genotype.geno_display_name ) 
    into genotype.geno_display_name,
        execute function zero_pad(new_genotype.geno_display_name ) 
    into genotype.geno_name_order
);
