#!/bin/sh

psql -d $DBNAME -f E_zfin_ensembl_gene.sql -f E_expression_gff3.sql -f E_phenotype_gff3.sql -f E_antibody_gff3.sql -f unload_mutants.sql

./generateGff3.groovy