select geno_zdb_id, geno_display_name, get_genotype_display(geno_zdb_id) as computed_display_name
 from genotype
 where trim(get_genotype_display(geno_zdb_id)) != trim(geno_display_name);
order by geno_zdb_id
