select geno_zdb_id, geno_display_name, get_genotype_display(geno_zdb_id)
 from genotype
 where get_genotype_display(geno_zdb_id) != geno_display_name;
