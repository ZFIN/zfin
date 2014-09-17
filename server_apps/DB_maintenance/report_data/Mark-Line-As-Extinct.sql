begin work ;

UPDATE genotype
SET    geno_is_extinct = 't'
WHERE  geno_zdb_id IN ( '$GENOID' );

SELECT geno_zdb_id,
  geno_is_extinct
FROM   genotype
WHERE  geno_zdb_id = '$GENOID';

commit work ;