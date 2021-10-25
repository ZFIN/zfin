-- This SQL basically queries for any rows in the genotype table where the display_name doesn't match 
-- the computed display_name (get_genotype_display).  Additionally, I'm filtering further to only rows
-- where the only difference between the computed name and the existing name is the space preceding
-- a semicolon.  Once only those rows are returned, we can update the display_name to match the computed name

UPDATE genotype
SET geno_display_name = subq2.computed_genotype_name FROM
( SELECT * FROM
        ( SELECT geno_zdb_id,
                geno_display_name AS current_genotype_name,
                get_genotype_display ( geno_zdb_id ) AS computed_genotype_name,
                REPLACE ( geno_display_name, ' ;', ';' ) AS space_removed
                FROM genotype ) AS subq
WHERE
        current_genotype_name <> computed_genotype_name AND
        space_removed = computed_genotype_name) as subq2

WHERE subq2.geno_zdb_id = genotype.geno_zdb_id
