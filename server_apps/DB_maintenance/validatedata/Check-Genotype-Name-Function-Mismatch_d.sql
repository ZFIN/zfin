SELECT
    geno_zdb_id,
    geno_display_name,
    get_genotype_display (geno_zdb_id) AS computed_display_name
FROM
    genotype
WHERE
    (trim(get_genotype_display (geno_zdb_id)) != trim(geno_display_name)
    OR trim(geno_display_name) = ''
    OR trim(get_genotype_display (geno_zdb_id)) = ''
    OR trim(geno_display_name) = '_')
  AND NOT
    -- IGNORE THIS ENTRY FOR NOW: ZFIN-7922
    -- DUE TO FEATURES THAT ARE ALLELES OF MULTIPLE GENES
    ( geno_zdb_id = 'ZDB-GENO-191211-5' and geno_display_name = 'mir183<sup>lri70/lri70</sup> ; mir96<sup>lri79/lri79</sup> ; mir182<sup>lri81/lri81</sup>')
ORDER BY
    geno_zdb_id