#!/bin/sh

psql -v ON_ERROR_STOP=1 -d $DBNAME -f E_zfin_ensembl_gene.sql -f E_expression_gff3.sql -f E_phenotype_gff3.sql -f E_antibody_gff3.sql -f unload_mutants.sql -f zfin_zmp_gff3.sql

./generateGff3.groovy

cd $SOURCEROOT
gradle createGff3Files

gzip zfin_genes.grcz12.gff3
gzip zfin_refseq.grcz12.gff3
cp zfin_genes*.gff3 /opt/zfin/www_homes/zfin.org/home/data_transfer/Downloads/.
cp zfin_ref*.gff3 /opt/zfin/www_homes/zfin.org/home/data_transfer/Downloads/.