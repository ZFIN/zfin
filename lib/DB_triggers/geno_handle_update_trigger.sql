create trigger geno_handle_update_trigger update of geno_handle
  on genotype referencing new as new_genotype
    for each row
    (
        execute function scrub_char(new_genotype.geno_handle ) 
    into genotype.geno_handle,
	execute procedure p_update_geno_nickname(new_genotype.geno_zdb_id,
		new_genotype.geno_handle)
);
