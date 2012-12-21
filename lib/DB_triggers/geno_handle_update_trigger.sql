create trigger geno_handle_update_trigger update of geno_handle
  on genotype referencing new as new_genotype
    for each row
    (
        execute function scrub_char(new_genotype.geno_handle ) 
    into genotype.geno_handle,
	execute procedure p_update_geno_nickname(new_genotype.geno_zdb_id,
		new_genotype.geno_handle),
        execute function update_geno_sort_order(new_genotype.geno_zdb_id)
	into genotype.geno_complexity_order
);
