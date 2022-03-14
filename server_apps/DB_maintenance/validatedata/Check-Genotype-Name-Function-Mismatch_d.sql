SELECT
    geno_zdb_id,
    geno_display_name,
    get_genotype_display (geno_zdb_id) AS computed_display_name
FROM
    genotype
WHERE
    trim(get_genotype_display (geno_zdb_id)) != trim(geno_display_name)
    OR trim(geno_display_name) = ''
    OR trim(get_genotype_display (geno_zdb_id)) = ''
ORDER BY
    geno_zdb_id